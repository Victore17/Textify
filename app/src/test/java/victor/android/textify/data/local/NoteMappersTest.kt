package victor.android.textify.data.local

import org.junit.Assert.assertEquals
import org.junit.Test
import victor.android.textify.domain.model.Note
import java.time.Instant

class NoteMappersTest {

    @Test
    fun `entidad a dominio conserva los datos`() {
        val entity = NoteEntity(id = 7, text = "Hola mundo", createdAt = 1_700_000_000_123)

        val note = entity.toDomain()

        assertEquals(7L, note.id)
        assertEquals("Hola mundo", note.text)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_123), note.createdAt)
    }

    @Test
    fun `dominio a entidad conserva los datos`() {
        val note = Note(id = 3, text = "Texto", createdAt = Instant.ofEpochMilli(42))

        val entity = note.toEntity()

        assertEquals(NoteEntity(id = 3, text = "Texto", createdAt = 42), entity)
    }

    @Test
    fun `ida y vuelta devuelve el mismo objeto`() {
        val note = Note(id = 9, text = "Ida y vuelta ✓", createdAt = Instant.ofEpochMilli(1_234_567))
        val entity = NoteEntity(id = 1, text = "otra", createdAt = 99)

        assertEquals(note, note.toEntity().toDomain())
        assertEquals(entity, entity.toDomain().toEntity())
    }
}
