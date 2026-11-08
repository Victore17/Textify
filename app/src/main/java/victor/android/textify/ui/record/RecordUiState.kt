package victor.android.textify.ui.record

import victor.android.textify.speech.SpeechError

/** Estado único de la pantalla de grabación y transcripción. */
sealed interface RecordUiState {
    /** Listo para grabar. */
    data object Idle : RecordUiState

    /** Escuchando; [partialText] es la transcripción en vivo (puede estar vacía). */
    data class Listening(val partialText: String = "") : RecordUiState

    /** Transcripción final, lista para guardar, compartir o cancelar. */
    data class Review(val text: String) : RecordUiState

    /** La sesión terminó en error. */
    data class Error(val error: SpeechError) : RecordUiState
}
