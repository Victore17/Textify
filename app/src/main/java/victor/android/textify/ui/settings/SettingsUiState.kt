package victor.android.textify.ui.settings

import victor.android.textify.domain.model.AppLanguage
import victor.android.textify.domain.model.AppTheme

/** Estado de la pantalla de ajustes. */
data class SettingsUiState(
    val language: AppLanguage = AppLanguage.ES_MX,
    val theme: AppTheme = AppTheme.SYSTEM,
)
