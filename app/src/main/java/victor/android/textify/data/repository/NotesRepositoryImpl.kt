package victor.android.textify.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import victor.android.textify.data.local.NoteDao
import victor.android.textify.data.local.toDomain
import victor.android.textify.data.local.toEntity
import victor.android.textify.di.IoDispatcher
import victor.android.textify.domain.model.EmptyNoteException
import victor.android.textify.domain.model.Note
import victor.android.textify.domain.model.isValidNoteText
import victor.android.textify.domain.repository.NotesRepository
import java.time.Clock
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class NotesRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
    private val clock: Clock,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : NotesRepository {

    override fun observeNotes(): Flow<List<Note>> =
        dao.observeAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun saveNote(text: String): Result<Note> {
        if (!isValidNoteText(text)) return Result.failure(EmptyNoteException())
        val note = Note(text = text.trim(), createdAt = clock.instant())
        return try {
            val id = withContext(ioDispatcher) { dao.insert(note.toEntity()) }
            Result.success(note.copy(id = id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(id: Long): Boolean =
        withContext(ioDispatcher) { dao.deleteById(id) > 0 }

    override suspend fun restoreNote(note: Note) =
        withContext(ioDispatcher) { dao.restore(note.toEntity()) }
}
