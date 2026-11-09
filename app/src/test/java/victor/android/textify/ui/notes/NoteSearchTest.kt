package victor.android.textify.ui.notes

import org.junit.Assert.assertEquals
import org.junit.Test
import victor.android.textify.domain.model.Note
import java.time.Instant

class NoteSearchTest {

    private val noteA = Note(id = 1, text = "Comprar leche", createdAt = Instant.EPOCH)
    private val noteB = Note(id = 2, text = "Llamar al doctor", createdAt = Instant.EPOCH)

    @Test
    fun `query vacio no filtra`() {
        assertEquals(listOf(noteA, noteB), filterNotes(listOf(noteA, noteB), ""))
    }

    @Test
    fun `sin coincidencias devuelve lista vacia`() {
        assertEquals(emptyList<Note>(), filterNotes(listOf(noteA, noteB), "zzz"))
    }

    @Test
    fun `coincidencia parcial devuelve solo las notas que contienen el texto`() {
        assertEquals(listOf(noteA), filterNotes(listOf(noteA, noteB), "leche"))
    }

    @Test
    fun `la busqueda no distingue mayusculas`() {
        assertEquals(listOf(noteB), filterNotes(listOf(noteA, noteB), "DOCTOR"))
    }
}
