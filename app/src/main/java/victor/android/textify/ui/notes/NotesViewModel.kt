package victor.android.textify.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import victor.android.textify.domain.model.Note
import victor.android.textify.domain.repository.NotesRepository
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _events = Channel<NotesEvent>(Channel.BUFFERED)
    val events: Flow<NotesEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<NotesUiState> =
        combine(notesRepository.observeNotes(), _searchQuery, ::filterNotes)
            .map { filtered -> if (filtered.isEmpty()) NotesUiState.Empty else NotesUiState.Content(filtered) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, NotesUiState.Loading)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDeleteNote(note: Note) {
        viewModelScope.launch {
            if (notesRepository.deleteNote(note.id)) {
                _events.send(NotesEvent.NoteDeleted(note))
            }
        }
    }

    fun onUndoDelete(note: Note) {
        viewModelScope.launch {
            notesRepository.restoreNote(note)
        }
    }
}
