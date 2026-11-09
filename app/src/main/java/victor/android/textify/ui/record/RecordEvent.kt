package victor.android.textify.ui.record

/** Eventos de un solo uso de la pantalla de grabación (p. ej. para un Snackbar). */
sealed interface RecordEvent {
    data object NoteSaved : RecordEvent
}
