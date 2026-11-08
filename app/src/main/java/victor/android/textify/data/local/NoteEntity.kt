package victor.android.textify.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [Index(value = ["created_at"])],
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    /** Milisegundos desde epoch (UTC). */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
