package victor.android.textify.speech

import android.speech.SpeechRecognizer

/**
 * Traduce un código de error de [SpeechRecognizer] a [SpeechError].
 *
 * Se usan los valores numéricos porque varias constantes solo existen en APIs recientes
 * (10-15 desde API 31-34) y `minSdk` es 26.
 */
fun mapSpeechError(code: Int): SpeechError = when (code) {
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT, // 1
    SpeechRecognizer.ERROR_NETWORK, // 2
    ERROR_SERVER_DISCONNECTED, // 11
    -> SpeechError.NETWORK_UNAVAILABLE

    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechError.NO_SPEECH // 6
    SpeechRecognizer.ERROR_NO_MATCH -> SpeechError.NO_MATCH // 7

    SpeechRecognizer.ERROR_RECOGNIZER_BUSY, // 8
    ERROR_TOO_MANY_REQUESTS, // 10
    -> SpeechError.BUSY

    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechError.PERMISSION_DENIED // 9

    ERROR_LANGUAGE_NOT_SUPPORTED, // 12
    ERROR_LANGUAGE_UNAVAILABLE, // 13
    -> SpeechError.LANGUAGE_NOT_SUPPORTED

    // ERROR_AUDIO (3), ERROR_SERVER (4), ERROR_CLIENT (5), 14, 15 y desconocidos.
    else -> SpeechError.OTHER
}

/**
 * Devuelve el mejor candidato de [candidates]: el primero no vacío, sin espacios sobrantes.
 * [SpeechRecognizer] ya entrega la lista ordenada de mayor a menor confianza.
 */
fun bestCandidate(candidates: List<String>?): String? =
    candidates.orEmpty().firstNotNullOfOrNull { it.trim().ifEmpty { null } }

internal const val ERROR_TOO_MANY_REQUESTS = 10
internal const val ERROR_SERVER_DISCONNECTED = 11
internal const val ERROR_LANGUAGE_NOT_SUPPORTED = 12
internal const val ERROR_LANGUAGE_UNAVAILABLE = 13
