package victor.android.textify.testutil

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import victor.android.textify.speech.SpeechError
import victor.android.textify.speech.SpeechState
import victor.android.textify.speech.SpeechToTextEngine

/** Motor de voz falso para tests: solo maneja texto y registra las llamadas recibidas. */
class FakeSpeechToTextEngine : SpeechToTextEngine {

    private val _state = MutableStateFlow<SpeechState>(SpeechState.Idle)
    override val state: StateFlow<SpeechState> = _state.asStateFlow()

    val startedLanguages = mutableListOf<String>()
    var stopCount = 0
        private set
    var released = false
        private set

    override fun start(languageTag: String) {
        startedLanguages += languageTag
        _state.value = SpeechState.Listening
    }

    override fun stop() {
        stopCount++
    }

    override fun release() {
        released = true
        _state.value = SpeechState.Idle
    }

    fun emitPartial(text: String) {
        _state.value = SpeechState.Partial(text)
    }

    fun emitResult(text: String) {
        _state.value = SpeechState.Result(text)
    }

    fun emitError(error: SpeechError) {
        _state.value = SpeechState.Error(error)
    }
}
