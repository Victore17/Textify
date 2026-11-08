package victor.android.textify.speech

import android.os.Bundle
import android.speech.SpeechRecognizer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecognitionStateListenerTest {

    private val states = mutableListOf<SpeechState>()
    private val listener = RecognitionStateListener { states += it }

    @Test
    fun `onReadyForSpeech produce Listening`() {
        listener.onReadyForSpeech(Bundle())

        assertEquals(listOf(SpeechState.Listening), states)
    }

    @Test
    fun `onPartialResults produce Partial con el mejor candidato`() {
        listener.onPartialResults(resultsBundle("hola", "ola"))

        assertEquals(listOf(SpeechState.Partial("hola")), states)
    }

    @Test
    fun `onPartialResults sin texto no cambia el estado`() {
        listener.onPartialResults(resultsBundle())
        listener.onPartialResults(Bundle())
        listener.onPartialResults(null)

        assertEquals(emptyList<SpeechState>(), states)
    }

    @Test
    fun `onResults produce Result con el mejor candidato`() {
        listener.onResults(resultsBundle("hola mundo", "ola mundo"))

        assertEquals(listOf(SpeechState.Result("hola mundo")), states)
    }

    @Test
    fun `onResults sin candidatos produce Result vacio`() {
        listener.onResults(resultsBundle())

        assertEquals(listOf(SpeechState.Result("")), states)
    }

    @Test
    fun `onError produce Error con el tipo traducido`() {
        listener.onError(SpeechRecognizer.ERROR_NO_MATCH)
        listener.onError(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)

        assertEquals(
            listOf(
                SpeechState.Error(SpeechError.NO_MATCH),
                SpeechState.Error(SpeechError.PERMISSION_DENIED),
            ),
            states,
        )
    }

    @Test
    fun `los buffers de audio se ignoran`() {
        listener.onBufferReceived(ByteArray(1024))
        listener.onRmsChanged(5f)
        listener.onBeginningOfSpeech()
        listener.onEndOfSpeech()

        assertEquals(emptyList<SpeechState>(), states)
    }

    private fun resultsBundle(vararg candidates: String) = Bundle().apply {
        putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf(*candidates))
    }
}
