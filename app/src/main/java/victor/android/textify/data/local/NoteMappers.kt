package victor.android.textify.data.local

import victor.android.textify.domain.model.Note
import java.time.Instant

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    text = text,
    createdAt = Instant.ofEpochMilli(createdAt),
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    text = text,
    createdAt = createdAt.toEpochMilli(),
)
