package victor.android.textify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /** Inserta una nota nueva y devuelve el id generado. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(note: NoteEntity): Long

    /** Todas las notas, la más reciente primero (a igual fecha, la de mayor id primero). */
    @Query("SELECT * FROM notes ORDER BY created_at DESC, id DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    /** Borra la nota con [id] y devuelve cuántas filas se borraron. */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    /** Reinserta una nota borrada conservando su id original. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(note: NoteEntity)
}
