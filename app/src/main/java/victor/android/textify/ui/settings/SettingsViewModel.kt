package victor.android.textify.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import victor.android.textify.domain.model.AppLanguage
import victor.android.textify.domain.model.AppTheme
import victor.android.textify.domain.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        combine(settingsRepository.language, settingsRepository.theme, ::SettingsUiState)
            .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }
}
