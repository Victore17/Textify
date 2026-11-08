package victor.android.textify.domain.model

import java.time.Instant

/**
 * Nota de texto guardada por el usuario.
 *
 * @property id identificador único; `0` significa que la nota aún no se ha guardado.
 * @property text contenido de la nota.
 * @property createdAt momento en que se creó la nota.
 */
data class Note(
    val id: Long = 0,
    val text: String,
    val createdAt: Instant,
)
