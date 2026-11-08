package victor.android.textify.speech

import android.speech.SpeechRecognizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpeechMappingTest {

    @Test
    fun `cada codigo de error de SpeechRecognizer se traduce al SpeechError esperado`() {
        val expected = mapOf(
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT to SpeechError.NETWORK_UNAVAILABLE, // 1
            SpeechRecognizer.ERROR_NETWORK to SpeechError.NETWORK_UNAVAILABLE, // 2
            SpeechRecognizer.ERROR_AUDIO to SpeechError.OTHER, // 3
            SpeechRecognizer.ERROR_SERVER to SpeechError.OTHER, // 4
            SpeechRecognizer.ERROR_CLIENT to SpeechError.OTHER, // 5
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT to SpeechError.NO_SPEECH, // 6
            SpeechRecognizer.ERROR_NO_MATCH to SpeechError.NO_MATCH, // 7
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY to SpeechError.BUSY, // 8
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS to SpeechError.PERMISSION_DENIED, // 9
            10 to SpeechError.BUSY, // ERROR_TOO_MANY_REQUESTS
            11 to SpeechError.NETWORK_UNAVAILABLE, // ERROR_SERVER_DISCONNECTED
            12 to SpeechError.LANGUAGE_NOT_SUPPORTED, // ERROR_LANGUAGE_NOT_SUPPORTED
            13 to SpeechError.LANGUAGE_NOT_SUPPORTED, // ERROR_LANGUAGE_UNAVAILABLE
            14 to SpeechError.OTHER, // ERROR_CANNOT_CHECK_SUPPORT
            15 to SpeechError.OTHER, // ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS
        )

        expected.forEach { (code, error) ->
            assertEquals("código $code", error, mapSpeechError(code))
        }
    }

    @Test
    fun `codigos desconocidos se traducen a OTHER`() {
        listOf(0, -1, 16, 999).forEach { code ->
            assertEquals("código $code", SpeechError.OTHER, mapSpeechError(code))
        }
    }

    @Test
    fun `mejor candidato de una lista nula o vacia es null`() {
        assertNull(bestCandidate(null))
        assertNull(bestCandidate(emptyList()))
    }

    @Test
    fun `mejor candidato de un solo elemento es ese elemento sin espacios sobrantes`() {
        assertEquals("hola", bestCandidate(listOf("  hola ")))
    }

    @Test
    fun `mejor candidato de varios es el primero`() {
        assertEquals("hola mundo", bestCandidate(listOf("hola mundo", "ola mundo", "hola mudo")))
    }

    @Test
    fun `se saltan los candidatos vacios`() {
        assertEquals("hola", bestCandidate(listOf("", "   ", "hola")))
        assertNull(bestCandidate(listOf("", " ")))
    }
}
