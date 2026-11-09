# Plan de desarrollo: Textify (app Android de voz a texto)

## 1. Descripción

**Nombre de la app: Textify.** Este nombre se usa en el `app_name` de `strings.xml`, el nombre del proyecto, el repositorio, el README, el ícono y la ficha de Google Play.

Textify graba la voz del usuario, la convierte a texto y se la muestra. Con el texto en pantalla, el usuario elige una de tres opciones:

1. **Guardar** el texto en sus notas.
2. **Compartir** el texto.
3. **Cancelar**: se borra el texto y la app queda lista para grabar de nuevo.

**Regla de privacidad principal:** el audio nunca se guarda. Solo se usa para obtener el texto. No se crea ningún archivo de audio, ni temporal ni permanente.

## 2. Reglas de trabajo (para Claude Code)

- Trabajar **una etapa a la vez**, en orden. No empezar la siguiente sin terminar la actual.
- Cada etapa incluye **sus propios unit tests** para el código que se agregó o cambió en ella.
- **Al terminar cada etapa se corren TODOS los tests**, los de esa etapa y los de todas las anteriores:
  ```
  ./gradlew testDebugUnitTest
  ```
- Una etapa solo se considera terminada si:
  - [ ] El código compila.
  - [ ] Los tests nuevos existen y pasan.
  - [ ] Todos los tests anteriores siguen pasando (sin regresiones).
- Si un test anterior falla, se arregla antes de continuar. No se borran ni se desactivan tests para que pase.
- Al cerrar cada etapa: marcar las casillas de este archivo, hacer commit con mensaje `etapa N: <resumen>` y anotar en la sección "Registro de avance" el resultado de los tests (cuántos pasaron).
- Antes de agregar una librería, revisar la versión estable actual y registrarla en `gradle/libs.versions.toml`.
- Todos los textos visibles al usuario van en `strings.xml` (español como idioma base, inglés como segundo idioma).

## 3. Stack

| Área | Herramienta |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navegación | Navigation Compose |
| Arquitectura | MVVM, flujo unidireccional (ViewModel + StateFlow), capas ui / domain / data |
| DI | Hilt |
| Base de datos | Room (con KSP) |
| Preferencias | DataStore |
| Asincronía | Coroutines + Flow |
| Voz a texto | `SpeechRecognizer` de Android (no se usa `MediaRecorder` ni `AudioRecord` para guardar nada) |
| Tests | JUnit4, kotlinx-coroutines-test, Turbine, MockK, Robolectric (para Room y clases con Android framework), Compose UI Test |
| Build | Version Catalog (`libs.versions.toml`), R8 en release |

Sugerencia de paquetes:

```
com.<tuapp>.textify
├── ui/         (pantallas, ViewModels, componentes, tema)
├── domain/     (modelos, interfaces de repositorio, casos de uso)
├── data/       (Room, DataStore, implementaciones)
├── speech/     (envoltorio de SpeechRecognizer)
└── di/         (módulos de Hilt)
```

---

## Etapa 0: Base del proyecto

**Objetivo:** proyecto que compila, con dependencias, tema y estructura listos.

**Tareas**
- Crear proyecto Compose llamado **Textify** (`app_name` = "Textify" en `strings.xml`, tema `TextifyTheme`) con `minSdk` razonable (ej. 26) y `targetSdk` el más reciente estable.
- Configurar `libs.versions.toml` con todas las dependencias del stack.
- Configurar Hilt, KSP, Room, DataStore, Navigation Compose.
- Configurar el tema Material 3 (claro/oscuro) y una `MainActivity` con `NavHost` vacío.
- Configurar dependencias de test (JUnit, coroutines-test, Turbine, MockK, Robolectric) y `unitTests.isIncludeAndroidResources = true`.
- Crear regla de test para el dispatcher principal (`MainDispatcherRule`) reutilizable.
- Crear `.gitignore`, repo en GitHub y `README.md` inicial.

**Unit tests de la etapa**
- `MainDispatcherRuleTest`: verifica que reemplaza y restaura `Dispatchers.Main`.
- Un test de humo que confirme que el módulo de Hilt de prueba se construye sin errores.

**Cierre:** correr `./gradlew testDebugUnitTest` (todos los tests).

---

## Etapa 1: Modelo de dominio y persistencia de notas

**Objetivo:** poder guardar, leer y borrar notas de texto en el dispositivo.

**Tareas**
- Modelo de dominio `Note(id, text, createdAt)`.
- Entidad Room `NoteEntity`, `NoteDao` (insertar, observar todas ordenadas por fecha descendente, borrar, restaurar), `AppDatabase`.
- Mapeadores entidad ↔ dominio.
- Interfaz `NotesRepository` en `domain` e implementación `NotesRepositoryImpl` en `data`.
- Proveer base de datos, DAO y repositorio con Hilt.
- Validación: no se guarda una nota vacía o solo con espacios.

**Unit tests de la etapa**
- Mapeadores: entidad → dominio y dominio → entidad conservan los datos.
- `NoteDao` (Room en memoria con Robolectric): insertar, leer ordenado por fecha, borrar, que una lista vacía emita lista vacía.
- `NotesRepositoryImpl`: guarda nota válida, rechaza nota vacía, expone el flujo de notas, borra.

**Cierre:** correr todos los tests.

---

## Etapa 2: Envoltorio del reconocedor de voz

**Objetivo:** una clase que convierte voz a texto y expone su estado como `Flow`, sin guardar audio.

**Tareas**
- Definir `SpeechState`: `Idle`, `Listening`, `Partial(text)`, `Result(text)`, `Error(type)`.
- Definir `SpeechError`: sin coincidencia, sin voz detectada, red no disponible, ocupado, permiso denegado, idioma no soportado, otro.
- Interfaz `SpeechToTextEngine` (`state: Flow<SpeechState>`, `start(languageTag)`, `stop()`, `release()`) para poder falsearla en tests.
- Implementación `AndroidSpeechToTextEngine` con `SpeechRecognizer`:
  - Usar reconocimiento en el dispositivo cuando esté disponible y respaldo al estándar.
  - Pedir resultados parciales para mostrar la transcripción en vivo.
  - Convertir los callbacks de `RecognitionListener` a estados.
  - Liberar el reconocedor en `release()` y evitar fugas.
- **Verificar que no se escribe ningún archivo de audio** (no usar `MediaRecorder`, `AudioRecord` ni guardar buffers a disco).
- Mapeo de códigos de error de Android a `SpeechError` en una función pura.

**Unit tests de la etapa**
- Mapeo de códigos de error de `SpeechRecognizer` a `SpeechError` (todos los códigos, incluido uno desconocido).
- Función que extrae el mejor resultado de la lista de candidatos (lista vacía, un elemento, varios).
- `FakeSpeechToTextEngine` en tests: verificar que emite la secuencia `Idle → Listening → Partial → Result`.
- Con Robolectric y un listener simulado: los callbacks `onPartialResults`, `onResults` y `onError` producen el estado correcto.

**Cierre:** correr todos los tests.

---

## Etapa 3: Permiso de micrófono

**Objetivo:** pedir `RECORD_AUDIO` correctamente en todos sus casos.

**Tareas**
- Declarar `RECORD_AUDIO` en el manifest (y `<queries>` para el servicio de reconocimiento de voz si hace falta).
- Modelo `MicPermissionState`: `Granted`, `Denied` (se puede volver a pedir), `PermanentlyDenied` (llevar a Ajustes).
- Lógica pura que decide el estado a partir de `granted` y `shouldShowRationale`.
- Componente Compose que pide el permiso, muestra una explicación previa y un botón "Abrir ajustes" cuando está denegado permanentemente.

**Unit tests de la etapa**
- Función de decisión de estado: todas las combinaciones de `granted` / `shouldShowRationale` / primera vez.
- ViewModel o clase de estado del permiso: transiciones correctas al recibir cada respuesta.

**Cierre:** correr todos los tests.

---

## Etapa 4: Pantalla de grabación y transcripción

**Objetivo:** el usuario graba, ve el texto en vivo y queda con el resultado final en pantalla.

**Tareas**
- `RecordViewModel` con un `UiState` único:
  - `Idle` (listo para grabar)
  - `Listening(partialText)`
  - `Review(text)` (transcripción final, con las tres opciones)
  - `Error(mensaje)`
- Eventos: `onRecordClick`, `onStopClick`, `onRetry`, etc.
- Pantalla Compose: botón de micrófono grande, animación mientras escucha, texto parcial en vivo, estados de error con botón de reintentar.
- Manejo de silencio: si el reconocedor termina sin resultado, mostrar el error "no se detectó voz" y permitir volver a intentar.
- Liberar el motor de voz al salir de la pantalla (`onCleared` / ciclo de vida).
- Conservar el estado ante rotación (`SavedStateHandle` para el texto en revisión).

**Unit tests de la etapa** (con `FakeSpeechToTextEngine`)
- Estado inicial es `Idle`.
- Al pulsar grabar pasa a `Listening` y llama a `start()` con el idioma correcto.
- Los resultados parciales actualizan `partialText`.
- El resultado final pasa a `Review(text)`.
- Un error del motor pasa a `Error` con el mensaje correcto.
- Resultado vacío → error "no se detectó voz", no `Review`.
- `onCleared` libera el motor.

**Cierre:** correr todos los tests.

---

## Etapa 5: Opciones sobre el texto (guardar, compartir, cancelar)

**Objetivo:** las tres acciones del estado `Review`.

**Tareas**
- **Guardar:** llama al repositorio, muestra confirmación (Snackbar) y regresa a `Idle`.
- **Compartir:** abre el selector del sistema con `Intent.ACTION_SEND` (`text/plain`). Crear una función que construya el `Intent` para poder probarla. No se comparte ningún archivo, solo texto.
- **Cancelar:** borra el texto en memoria y vuelve a `Idle`, listo para grabar otra vez. Si hay texto, pedir confirmación solo si se decide que conviene (decisión de UX documentada aquí).
- Manejar fallo al guardar (mostrar error, mantener el texto para no perderlo).
- Tras compartir, mantener el texto en pantalla para que pueda también guardarlo o cancelar.

**Unit tests de la etapa**
- Guardar: llama al repositorio con el texto exacto, emite evento de éxito, vuelve a `Idle`.
- Guardar con fallo del repositorio: queda en `Review` con el texto intacto y muestra error.
- Cancelar: vuelve a `Idle`, el texto queda vacío, no se llama al repositorio.
- Después de cancelar se puede grabar de nuevo (ciclo completo `Idle → Listening → Review → Idle → Listening`).
- Constructor del `Intent` de compartir: acción, tipo MIME y extra de texto correctos (Robolectric).
- Prueba de que ningún camino del flujo escribe archivos de audio (el `FakeSpeechToTextEngine` solo maneja texto y no hay dependencia de almacenamiento en el ViewModel).

**Cierre:** correr todos los tests.

---

## Etapa 6: Lista de notas guardadas

**Objetivo:** ver, buscar y borrar las notas guardadas.

**Tareas**
- `NotesViewModel` con `UiState`: `Loading`, `Empty`, `Content(notes)`.
- Pantalla de lista con fecha y texto, estado vacío con mensaje y atajo a grabar.
- Pantalla o diálogo de detalle: ver nota completa, compartir, copiar, borrar.
- Búsqueda por texto (filtro en el ViewModel).
- Borrar con **deshacer** (Snackbar) usando restauración en el repositorio.
- Navegación entre grabar y notas (barra inferior o botones).

**Unit tests de la etapa**
- Estado `Empty` con lista vacía y `Content` con notas.
- Orden: la más reciente primero.
- Búsqueda: sin coincidencias, coincidencia parcial, sin distinguir mayúsculas.
- Borrar elimina la nota; deshacer la restaura.
- Compartir una nota usa la misma función de `Intent` de la etapa 5.

**Cierre:** correr todos los tests.

---

## Etapa 7: Ajustes (idioma y tema)

**Objetivo:** elegir el idioma de reconocimiento y el tema.

**Tareas**
- DataStore con `SettingsRepository`: idioma de dictado (es-MX, en-US como mínimo) y tema (sistema / claro / oscuro).
- Pantalla de ajustes.
- El `RecordViewModel` lee el idioma guardado al iniciar la grabación.
- Detectar si el idioma elegido no está disponible en el dispositivo y mostrar aviso claro.
- Sección "Privacidad" en la pantalla de ajustes que explique que el audio no se guarda.

**Unit tests de la etapa**
- `SettingsRepository`: valor por defecto, guardar y leer idioma, guardar y leer tema (DataStore con archivo temporal).
- `RecordViewModel` usa el idioma de los ajustes al llamar a `start()`.
- Cambiar el idioma en ajustes se refleja en la siguiente grabación.
- Idioma no disponible produce el estado de error esperado.

**Cierre:** correr todos los tests.

---

## Etapa 8: Pulido y calidad

**Objetivo:** que la app se sienta terminada y sea robusta.

**Tareas**
- Accesibilidad: `contentDescription`, tamaños táctiles mínimos de 48dp, contraste, soporte de lector de pantalla.
- Revisar rotación, modo oscuro, pantallas grandes y texto grande.
- Manejar interrupciones: llamada entrante, app en segundo plano mientras escucha (detener el reconocimiento y volver a `Idle` sin perder el texto ya transcrito).
- Pantalla de carga/animaciones sutiles, ícono de la app.
- Revisar que no haya fugas de memoria (liberación del reconocedor) y que no se escriba ningún archivo de audio en el almacenamiento.
- Opcional: pruebas de UI con Compose Test del flujo principal.

**Unit tests de la etapa**
- Pasar a segundo plano mientras escucha: se detiene el motor y el estado vuelve a `Idle` o conserva el texto parcial según lo definido.
- Doble toque rápido en grabar no inicia dos sesiones.
- Estados de error se limpian al reintentar.
- Si se agregan pruebas de UI: flujo `grabar → ver texto → guardar` con el motor falso.

**Cierre:** correr todos los tests y revisar cobertura (objetivo sugerido: ViewModels y repositorios sobre 80%).

---

## Etapa 9: Publicación en Google Play

**Objetivo:** app publicada.

**Tareas**
- Activar R8/minify en release y verificar que la app funciona con la versión firmada.
- Generar AAB firmado y guardar la keystore de forma segura (con respaldo).
- Política de privacidad publicada en una URL pública. Debe decir que se usa el micrófono solo para convertir voz a texto, que el audio no se guarda y, si el reconocimiento usa servidores de Google, indicarlo.
- Formulario de Seguridad de datos de Play Console, coherente con la política.
- Nombre en la ficha: "Textify". Verificar en Play Console que el nombre esté disponible y no choque con otra app.
- Capturas, ícono, descripción y texto corto en español e inglés.
- Prueba cerrada con al menos 12 testers durante 14 días si la cuenta personal es nueva (confirmar el requisito vigente en la consola).
- Pasar a producción.

**Tests de la etapa**
- Correr todos los tests una última vez sobre la rama de release.
- Prueba manual en 2 o 3 dispositivos reales, con y sin internet.

---

## Registro de avance

| Etapa | Estado | Tests pasando | Fecha |
|---|---|---|---|
| 0. Base del proyecto | ☑ | 5/5 | 2026-11-08 |
| 1. Modelo y persistencia | ☑ | 25/25 | 2026-11-08 |
| 2. Motor de voz | ☑ | 55/55 | 2026-11-08 |
| 3. Permiso de micrófono | ☑ | 65/65 | 2026-11-08 |
| 4. Grabación y transcripción | ☑ | 75/75 | 2026-11-08 |
| 5. Guardar / compartir / cancelar | ☑ | 82/82 | 2026-11-08 |
| 6. Lista de notas | ☑ | 93/93 | 2026-11-08 |
| 7. Ajustes | ☑ | 103/103 | 2026-11-08 |
| 8. Pulido y calidad | ☑ | 108/108 | 2026-11-08 |
| 9. Publicación | ☐ | | |

## Decisiones abiertas (resolver al llegar a la etapa)

- ~~¿Pedir confirmación al cancelar un texto? (etapa 5)~~ **Resuelto:** no se pide confirmación; "Cancelar" borra el texto en memoria de inmediato y vuelve a `Idle`. Es texto sin guardar y de bajo costo de recuperación (basta con grabar de nuevo), así que una confirmación solo añadiría un paso sin beneficio real.
- ¿Dictado continuo (reiniciar el reconocedor tras silencios) o una sola frase por grabación? (etapa 4)
- ¿Idiomas adicionales además de es-MX y en-US? (etapa 7)
- ~~¿Reconocimiento solo en el dispositivo o con respaldo a servidores de Google? (etapa 2)~~ **Resuelto:** se usa el reconocedor en el dispositivo (API 31+) cuando existe; si no existe o no tiene el idioma, se recurre al reconocedor estándar del sistema, que puede enviar el audio a servidores de Google para transcribirlo. La política de privacidad (etapa 9) y la sección de privacidad de ajustes (etapa 7) deben decirlo.
