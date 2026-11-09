package victor.android.textify.ui.notes

import victor.android.textify.domain.model.Note

/** Eventos de un solo uso de la pantalla de notas (p. ej. para un Snackbar con "deshacer"). */
sealed interface NotesEvent {
    data class NoteDeleted(val note: Note) : NotesEvent
}
