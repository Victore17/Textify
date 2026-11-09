package victor.android.textify.testutil

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import victor.android.textify.domain.model.AppLanguage
import victor.android.textify.domain.model.AppTheme
import victor.android.textify.domain.repository.SettingsRepository

/** [SettingsRepository] falso para tests: mantiene los ajustes en memoria. */
class FakeSettingsRepository(
    initialLanguage: AppLanguage = AppLanguage.ES_MX,
    initialTheme: AppTheme = AppTheme.SYSTEM,
) : SettingsRepository {

    private val _language = MutableStateFlow(initialLanguage)
    override val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _theme = MutableStateFlow(initialTheme)
    override val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        _language.value = language
    }

    override suspend fun setTheme(theme: AppTheme) {
        _theme.value = theme
    }
}
