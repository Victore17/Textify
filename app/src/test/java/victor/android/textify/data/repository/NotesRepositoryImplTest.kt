package victor.android.textify.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.textify.data.local.NoteDao
import victor.android.textify.data.local.NoteEntity
import victor.android.textify.domain.model.EmptyNoteException
import victor.android.textify.domain.model.Note
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class NotesRepositoryImplTest {

    private val now = Instant.ofEpochMilli(1_700_000_000_000)
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val dao = mockk<NoteDao>(relaxed = true)
    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = NotesRepositoryImpl(dao, clock, dispatcher)

    @Test
    fun `guardar nota valida inserta con texto recortado y fecha actual`() = runTest(dispatcher) {
        val inserted = slot<NoteEntity>()
        coEvery { dao.insert(capture(inserted)) } returns 5L

        val result = repository.saveNote("  Hola mundo  ")

        assertEquals(Note(id = 5, text = "Hola mundo", createdAt = now), result.getOrThrow())
        assertEquals(NoteEntity(id = 0, text = "Hola mundo", createdAt = now.toEpochMilli()), inserted.captured)
    }

    @Test
    fun `rechaza nota vacia sin tocar la base`() = runTest(dispatcher) {
        val result = repository.saveNote("")

        assertTrue(result.exceptionOrNull() is EmptyNoteException)
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `rechaza nota solo con espacios sin tocar la base`() = runTest(dispatcher) {
        val result = repository.saveNote("   \n\t ")

        assertTrue(result.exceptionOrNull() is EmptyNoteException)
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `un fallo de la base se devuelve como Result failure`() = runTest(dispatcher) {
        coEvery { dao.insert(any()) } throws IllegalStateException("disco lleno")

        val result = repository.saveNote("texto")

        // Las coroutines pueden copiar la excepción al recuperar el stack trace: se compara tipo y mensaje.
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalStateException)
        assertEquals("disco lleno", error?.message)
    }

    @Test
    fun `expone el flujo de notas mapeado a dominio`() = runTest(dispatcher) {
        every { dao.observeAll() } returns flowOf(
            emptyList(),
            listOf(
                NoteEntity(id = 2, text = "nueva", createdAt = 200),
                NoteEntity(id = 1, text = "vieja", createdAt = 100),
            ),
        )

        repository.observeNotes().test {
            assertEquals(emptyList<Note>(), awaitItem())
            assertEquals(
                listOf(
                    Note(id = 2, text = "nueva", createdAt = Instant.ofEpochMilli(200)),
                    Note(id = 1, text = "vieja", createdAt = Instant.ofEpochMilli(100)),
                ),
                awaitItem(),
            )
            awaitComplete()
        }
    }

    @Test
    fun `borrar delega en el dao e indica si existia`() = runTest(dispatcher) {
        coEvery { dao.deleteById(4) } returns 1
        coEvery { dao.deleteById(99) } returns 0

        assertTrue(repository.deleteNote(4))
        assertFalse(repository.deleteNote(99))
        coVerify { dao.deleteById(4) }
    }

    @Test
    fun `restaurar reinserta la nota conservando id y fecha`() = runTest(dispatcher) {
        val note = Note(id = 8, text = "volver", createdAt = Instant.ofEpochMilli(300))

        repository.restoreNote(note)

        coVerify { dao.restore(NoteEntity(id = 8, text = "volver", createdAt = 300)) }
    }
}
