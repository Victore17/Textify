package victor.android.textify.ui.record

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import victor.android.textify.speech.SpeechError
import victor.android.textify.testutil.FakeSpeechToTextEngine
import victor.android.textify.testutil.MainDispatcherRule

class RecordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val engine = FakeSpeechToTextEngine()

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        RecordViewModel(engine, savedStateHandle)

    @Test
    fun `estado inicial es Idle`() {
        val viewModel = viewModel()

        assertEquals(RecordUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `al pulsar grabar pasa a Listening y llama a start con el idioma correcto`() {
        val viewModel = viewModel()

        viewModel.onRecordClick()

        assertEquals(RecordUiState.Listening(), viewModel.uiState.value)
        assertEquals(listOf(RecordViewModel.DEFAULT_LANGUAGE_TAG), engine.startedLanguages)
    }

    @Test
    fun `los resultados parciales actualizan el texto parcial`() {
        val viewModel = viewModel()
        viewModel.onRecordClick()

        engine.emitPartial("hola")

        assertEquals(RecordUiState.Listening("hola"), viewModel.uiState.value)
    }

    @Test
    fun `el resultado final pasa a Review`() {
        val viewModel = viewModel()
        viewModel.onRecordClick()

        engine.emitResult("hola mundo")

        assertEquals(RecordUiState.Review("hola mundo"), viewModel.uiState.value)
    }

    @Test
    fun `un error del motor pasa a Error con el mensaje correcto`() {
        val viewModel = viewModel()
        viewModel.onRecordClick()

        engine.emitError(SpeechError.NETWORK_UNAVAILABLE)

        assertEquals(RecordUiState.Error(SpeechError.NETWORK_UNAVAILABLE), viewModel.uiState.value)
    }

    @Test
    fun `resultado vacio pasa a Error no se detecto voz y no a Review`() {
        val viewModel = viewModel()
        viewModel.onRecordClick()

        engine.emitResult("   ")

        assertEquals(RecordUiState.Error(SpeechError.NO_SPEECH), viewModel.uiState.value)
    }

    @Test
    fun `onStopClick delega en el motor`() {
        val viewModel = viewModel()
        viewModel.onRecordClick()

        viewModel.onStopClick()

        assertEquals(1, engine.stopCount)
    }

    @Test
    fun `onRetry vuelve a Idle y permite grabar de nuevo`() {
        val viewModel = viewModel()
        viewModel.onRecordClick()
        engine.emitResult("hola")

        viewModel.onRetry()

        assertEquals(RecordUiState.Idle, viewModel.uiState.value)

        viewModel.onRecordClick()
        assertEquals(RecordUiState.Listening(), viewModel.uiState.value)
        assertEquals(2, engine.startedLanguages.size)
    }

    @Test
    fun `restaura el texto en revision desde SavedStateHandle`() {
        val savedStateHandle = SavedStateHandle(mapOf("review_text" to "texto guardado"))

        val viewModel = viewModel(savedStateHandle)

        assertEquals(RecordUiState.Review("texto guardado"), viewModel.uiState.value)
    }

    @Test
    fun `onCleared libera el motor`() {
        val viewModel = viewModel()

        viewModel.onCleared()

        assertTrue(engine.released)
    }
}
