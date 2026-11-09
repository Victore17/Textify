package victor.android.textify.ui.notes

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import victor.android.textify.domain.model.Note
import victor.android.textify.domain.repository.NotesRepository
import victor.android.textify.testutil.MainDispatcherRule
import java.time.Instant

class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noteA = Note(id = 1, text = "primera nota", createdAt = Instant.ofEpochMilli(200))
    private val noteB = Note(id = 2, text = "segunda nota", createdAt = Instant.ofEpochMilli(100))

    private val notesFlow = MutableStateFlow<List<Note>>(emptyList())
    private val notesRepository = mockk<NotesRepository> {
        every { observeNotes() } returns notesFlow
    }

    private fun viewModel() = NotesViewModel(notesRepository)

    @Test
    fun `lista vacia produce estado Empty`() {
        val viewModel = viewModel()

        assertEquals(NotesUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `con notas produce Content en el orden del repositorio`() {
        notesFlow.value = listOf(noteA, noteB)

        val viewModel = viewModel()

        assertEquals(NotesUiState.Content(listOf(noteA, noteB)), viewModel.uiState.value)
    }

    @Test
    fun `buscar sin coincidencias produce Empty`() {
        notesFlow.value = listOf(noteA, noteB)
        val viewModel = viewModel()

        viewModel.onSearchQueryChange("zzz")

        assertEquals(NotesUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `buscar con coincidencia parcial filtra las notas`() {
        notesFlow.value = listOf(noteA, noteB)
        val viewModel = viewModel()

        viewModel.onSearchQueryChange("segunda")

        assertEquals(NotesUiState.Content(listOf(noteB)), viewModel.uiState.value)
    }

    @Test
    fun `buscar sin distinguir mayusculas`() {
        notesFlow.value = listOf(noteA, noteB)
        val viewModel = viewModel()

        viewModel.onSearchQueryChange("PRIMERA")

        assertEquals(NotesUiState.Content(listOf(noteA)), viewModel.uiState.value)
    }

    @Test
    fun `borrar elimina la nota y emite evento para deshacer`() = runTest {
        notesFlow.value = listOf(noteA, noteB)
        coEvery { notesRepository.deleteNote(noteA.id) } answers {
            notesFlow.value = notesFlow.value.filterNot { it.id == noteA.id }
            true
        }
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onDeleteNote(noteA)
            assertEquals(NotesEvent.NoteDeleted(noteA), awaitItem())
        }
        assertEquals(NotesUiState.Content(listOf(noteB)), viewModel.uiState.value)
    }

    @Test
    fun `deshacer restaura la nota borrada`() {
        notesFlow.value = listOf(noteA, noteB)
        coEvery { notesRepository.deleteNote(noteA.id) } answers {
            notesFlow.value = notesFlow.value.filterNot { it.id == noteA.id }
            true
        }
        coEvery { notesRepository.restoreNote(noteA) } answers {
            notesFlow.value = listOf(noteA, noteB)
        }
        val viewModel = viewModel()
        viewModel.onDeleteNote(noteA)

        viewModel.onUndoDelete(noteA)

        assertEquals(NotesUiState.Content(listOf(noteA, noteB)), viewModel.uiState.value)
    }
}
