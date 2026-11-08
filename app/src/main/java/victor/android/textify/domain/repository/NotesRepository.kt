package victor.android.textify.domain.repository

import kotlinx.coroutines.flow.Flow
import victor.android.textify.domain.model.Note

/** Acceso a las notas guardadas en el dispositivo. */
interface NotesRepository {

    /** Flujo de todas las notas, la más reciente primero. */
    fun observeNotes(): Flow<List<Note>>

    /**
     * Guarda una nota nueva con [text] (sin espacios al inicio ni al final).
     *
     * @return la nota guardada, o un fallo con
     * [victor.android.textify.domain.model.EmptyNoteException] si el texto está vacío.
     */
    suspend fun saveNote(text: String): Result<Note>

    /** Borra la nota con [id]. Devuelve `true` si existía. */
    suspend fun deleteNote(id: Long): Boolean

    /** Vuelve a insertar una nota borrada conservando su id y fecha (para "deshacer"). */
    suspend fun restoreNote(note: Note)
}
