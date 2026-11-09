package victor.android.textify.ui.record

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import victor.android.textify.domain.model.AppLanguage
import victor.android.textify.domain.model.Note
import victor.android.textify.domain.repository.NotesRepository
import victor.android.textify.domain.repository.SettingsRepository
import victor.android.textify.speech.SpeechError
import victor.android.textify.testutil.FakeSettingsRepository
import victor.android.textify.testutil.FakeSpeechToTextEngine
import victor.android.textify.testutil.MainDispatcherRule
import java.time.Instant

class RecordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val engine = FakeSpeechToTextEngine()
    private val notesRepository = mockk<NotesRepository>()
    private val settingsRepository = FakeSettingsRepository()

    private fun viewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        settingsRepository: SettingsRepository = this.settingsRepository,
    ) = RecordViewModel(engine, notesRepository, settingsRepository, savedStateHandle)

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
        assertEquals(listOf(AppLanguage.ES_MX.tag), engine.startedLanguages)
    }

    @Test
    fun `usa el idioma guardado en los ajustes al llamar a start`() {
        val settings = FakeSettingsRepository(initialLanguage = AppLanguage.EN_US)
        val viewModel = viewModel(settingsRepository = settings)

        viewModel.onRecordClick()

        assertEquals(listOf(AppLanguage.EN_US.tag), engine.startedLanguages)
    }

    @Test
    fun `cambiar el idioma en ajustes se refleja en la siguiente grabacion`() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = viewModel(settingsRepository = settings)
        viewModel.onRecordClick()
        engine.emitResult("hola")

        settings.setLanguage(AppLanguage.EN_US)
        viewModel.onRecordClick()

        assertEquals(listOf(AppLanguage.ES_MX.tag, AppLanguage.EN_US.tag), engine.startedLanguages)
    }

    @Test
    fun `idioma no disponible produce el estado de error esperado`() {
        val viewModel = viewModel()
        viewModel.onRecordClick()

        engine.emitError(SpeechError.LANGUAGE_NOT_SUPPORTED)

        assertEquals(RecordUiState.Error(SpeechError.LANGUAGE_NOT_SUPPORTED), viewModel.uiState.value)
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

    @Test
    fun `guardar llama al repositorio con el texto exacto y vuelve a Idle`() = runTest {
        coEvery { notesRepository.saveNote("hola mundo") } returns
            Result.success(Note(id = 1, text = "hola mundo", createdAt = Instant.EPOCH))
        val viewModel = viewModel()
        viewModel.onRecordClick()
        engine.emitResult("hola mundo")

        viewModel.onSaveClick()

        coVerify(exactly = 1) { notesRepository.saveNote("hola mundo") }
        assertEquals(RecordUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `guardar emite el evento de nota guardada`() = runTest {
        coEvery { notesRepository.saveNote(any()) } returns
            Result.success(Note(id = 1, text = "hola", createdAt = Instant.EPOCH))
        val viewModel = viewModel()
        viewModel.onRecordClick()
        engine.emitResult("hola")

        viewModel.events.test {
            viewModel.onSaveClick()
            assertEquals(RecordEvent.NoteSaved, awaitItem())
        }
    }

    @Test
    fun `guardar con fallo del repositorio queda en Review con el texto intacto y muestra error`() = runTest {
        coEvery { notesRepository.saveNote("hola") } returns Result.failure(IllegalStateException("fallo"))
        val viewModel = viewModel()
        viewModel.onRecordClick()
        engine.emitResult("hola")

        viewModel.onSaveClick()

        assertEquals(RecordUiState.Review("hola", isSaving = false, saveError = true), viewModel.uiState.value)
    }

    @Test
    fun `cancelar vuelve a Idle sin llamar al repositorio`() {
        val viewModel = viewModel()
        viewModel.onRecordClick()
        engine.emitResult("hola")

        viewModel.onCancelClick()

        assertEquals(RecordUiState.Idle, viewModel.uiState.value)
        coVerify(exactly = 0) { notesRepository.saveNote(any()) }
    }

    @Test
    fun `despues de cancelar se puede grabar de nuevo`() {
        val viewModel = viewModel()
        viewModel.onRecordClick()
        engine.emitResult("hola")
        viewModel.onCancelClick()

        viewModel.onRecordClick()
        engine.emitPartial("de nuevo")

        assertEquals(RecordUiState.Listening("de nuevo"), viewModel.uiState.value)
    }
}
