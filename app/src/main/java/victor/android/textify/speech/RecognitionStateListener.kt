package victor.android.textify.speech

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer

/**
 * Convierte los callbacks de [RecognitionListener] en [SpeechState] y los entrega a [onState].
 *
 * Nunca guarda audio: [onBufferReceived] se ignora a propósito.
 */
class RecognitionStateListener(
    private val onState: (SpeechState) -> Unit,
) : RecognitionListener {

    override fun onReadyForSpeech(params: Bundle?) = onState(SpeechState.Listening)

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(rmsdB: Float) = Unit

    /** Fragmentos de audio crudo. No se leen ni se guardan (regla de privacidad). */
    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() = Unit

    override fun onError(error: Int) = onState(SpeechState.Error(mapSpeechError(error)))

    override fun onPartialResults(partialResults: Bundle?) {
        val text = bestCandidate(partialResults.candidates()) ?: return
        onState(SpeechState.Partial(text))
    }

    override fun onResults(results: Bundle?) =
        onState(SpeechState.Result(bestCandidate(results.candidates()).orEmpty()))

    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    private fun Bundle?.candidates(): List<String>? =
        this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
}
