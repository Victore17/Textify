# Textify

App Android que convierte tu voz en texto. Con el texto en pantalla puedes **guardarlo** en tus notas, **compartirlo** o **cancelarlo** para grabar de nuevo.

**Privacidad:** el audio nunca se guarda. Solo se usa para obtener el texto; no se crea ningún archivo de audio.

## Stack

Kotlin · Jetpack Compose + Material 3 · Navigation Compose · MVVM (ViewModel + StateFlow) · Hilt · Room (KSP) · DataStore · Coroutines/Flow · `SpeechRecognizer` de Android.

Tests: JUnit4, kotlinx-coroutines-test, Turbine, MockK, Robolectric, Compose UI Test.

## Requisitos

- Android Studio reciente, JDK 17+ (el proyecto usa el toolchain 25)
- `minSdk` 26, `targetSdk` 37

## Comandos

```bash
./gradlew assembleDebug        # compilar
./gradlew installDebug         # instalar en el dispositivo conectado
./gradlew testDebugUnitTest    # correr todos los unit tests
```

## Estructura

```
victor.android.textify
├── ui/       pantallas, ViewModels, navegación, tema
├── domain/   modelos, interfaces de repositorio, casos de uso
├── data/     Room, DataStore, implementaciones
├── speech/   envoltorio de SpeechRecognizer
└── di/       módulos de Hilt
```

El plan de desarrollo por etapas está en [`app/plan.md`](app/plan.md).
