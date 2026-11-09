package victor.android.textify.ui.record

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import victor.android.textify.domain.repository.NotesRepository
import victor.android.textify.domain.repository.SettingsRepository
import victor.android.textify.speech.SpeechError
import victor.android.textify.speech.SpeechState
import victor.android.textify.speech.SpeechToTextEngine
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val speechToTextEngine: SpeechToTextEngine,
    private val notesRepository: NotesRepository,
    private val settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        savedStateHandle.get<String>(KEY_REVIEW_TEXT)?.let { RecordUiState.Review(it) }
            ?: RecordUiState.Idle,
    )
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private val _events = Channel<RecordEvent>(Channel.BUFFERED)
    val events: Flow<RecordEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            speechToTextEngine.state.collect(::onSpeechState)
        }
    }

    fun onRecordClick() {
        savedStateHandle[KEY_REVIEW_TEXT] = null
        _uiState.value = RecordUiState.Listening()
        viewModelScope.launch {
            speechToTextEngine.start(settingsRepository.language.first().tag)
        }
    }

    fun onStopClick() {
        speechToTextEngine.stop()
    }

    fun onRetry() {
        savedStateHandle[KEY_REVIEW_TEXT] = null
        _uiState.value = RecordUiState.Idle
    }

    fun onSaveClick() {
        val review = _uiState.value as? RecordUiState.Review ?: return
        _uiState.value = review.copy(isSaving = true, saveError = false)
        viewModelScope.launch {
            val result = notesRepository.saveNote(review.text)
            if (result.isSuccess) {
                savedStateHandle[KEY_REVIEW_TEXT] = null
                _uiState.value = RecordUiState.Idle
                _events.send(RecordEvent.NoteSaved)
            } else {
                _uiState.value = review.copy(isSaving = false, saveError = true)
            }
        }
    }

    fun onCancelClick() {
        savedStateHandle[KEY_REVIEW_TEXT] = null
        _uiState.value = RecordUiState.Idle
    }

    public override fun onCleared() {
        speechToTextEngine.release()
    }

    private fun onSpeechState(state: SpeechState) {
        when (state) {
            is SpeechState.Idle -> Unit
            is SpeechState.Listening -> _uiState.value = RecordUiState.Listening()
            is SpeechState.Partial -> _uiState.value = RecordUiState.Listening(state.text)
            is SpeechState.Result -> onResult(state.text)
            is SpeechState.Error -> _uiState.value = RecordUiState.Error(state.type)
        }
    }

    private fun onResult(rawText: String) {
        val text = rawText.trim()
        _uiState.value = if (text.isEmpty()) {
            RecordUiState.Error(SpeechError.NO_SPEECH)
        } else {
            savedStateHandle[KEY_REVIEW_TEXT] = text
            RecordUiState.Review(text)
        }
    }

    companion object {
        private const val KEY_REVIEW_TEXT = "review_text"
    }
}
