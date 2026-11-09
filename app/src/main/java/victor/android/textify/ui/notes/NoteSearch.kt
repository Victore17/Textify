package victor.android.textify.ui.notes

import victor.android.textify.domain.model.Note

/** Filtra [notes] por las que contienen [query] en su texto, sin distinguir mayúsculas. */
fun filterNotes(notes: List<Note>, query: String): List<Note> =
    if (query.isBlank()) notes else notes.filter { it.text.contains(query, ignoreCase = true) }
