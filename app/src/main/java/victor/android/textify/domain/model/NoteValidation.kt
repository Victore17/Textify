package victor.android.textify.domain.model

/** Una nota es válida si su texto tiene al menos un carácter que no sea espacio en blanco. */
fun isValidNoteText(text: String): Boolean = text.isNotBlank()

/** Se lanza al intentar guardar una nota vacía o compuesta solo por espacios. */
class EmptyNoteException : IllegalArgumentException("El texto de la nota no puede estar vacío")
