package victor.android.textify.speech

/** Estado del reconocimiento de voz. Solo contiene texto: el audio nunca sale del reconocedor. */
sealed interface SpeechState {
    /** Sin sesión activa. */
    data object Idle : SpeechState

    /** El reconocedor está escuchando y aún no hay texto. */
    data object Listening : SpeechState

    /** Transcripción parcial mientras el usuario habla. */
    data class Partial(val text: String) : SpeechState

    /** Transcripción final de la sesión (puede ser vacía si no hubo candidatos). */
    data class Result(val text: String) : SpeechState

    /** La sesión terminó con un error. */
    data class Error(val type: SpeechError) : SpeechState
}

enum class SpeechError {
    NO_MATCH,
    NO_SPEECH,
    NETWORK_UNAVAILABLE,
    BUSY,
    PERMISSION_DENIED,
    LANGUAGE_NOT_SUPPORTED,
    OTHER,
}
