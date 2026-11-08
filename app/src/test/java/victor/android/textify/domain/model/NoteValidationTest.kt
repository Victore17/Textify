package victor.android.textify.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteValidationTest {

    @Test
    fun `texto vacio no es valido`() = assertFalse(isValidNoteText(""))

    @Test
    fun `texto solo con espacios no es valido`() = assertFalse(isValidNoteText("  \t\n "))

    @Test
    fun `texto con contenido es valido`() = assertTrue(isValidNoteText("  hola "))
}
