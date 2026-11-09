package victor.android.textify.ui.notes

import victor.android.textify.domain.model.Note

/** Estado único de la pantalla de lista de notas. */
sealed interface NotesUiState {
    /** Las notas aún no se han cargado. */
    data object Loading : NotesUiState

    /** No hay notas que mostrar (sin notas guardadas, o ninguna coincide con la búsqueda). */
    data object Empty : NotesUiState

    /** [notes] en el orden que entrega el repositorio (la más reciente primero). */
    data class Content(val notes: List<Note>) : NotesUiState
}
