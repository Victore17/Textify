package victor.android.textify.speech

import kotlinx.coroutines.flow.StateFlow

/** Convierte voz a texto. Se abstrae en una interfaz para poder falsearla en tests. */
interface SpeechToTextEngine {

    /** Estado actual del reconocimiento. Empieza en [SpeechState.Idle]. */
    val state: StateFlow<SpeechState>

    /** Empieza a escuchar en el idioma [languageTag] (BCP 47, p. ej. "es-MX"). */
    fun start(languageTag: String)

    /** Deja de escuchar; el reconocedor entrega el resultado de lo que ya oyó. */
    fun stop()

    /** Libera el reconocedor. Debe llamarse al salir de la pantalla. */
    fun release()
}
