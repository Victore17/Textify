package victor.android.textify.domain.repository

import kotlinx.coroutines.flow.Flow
import victor.android.textify.domain.model.AppLanguage
import victor.android.textify.domain.model.AppTheme

/** Ajustes persistentes de la app: idioma de dictado y tema visual. */
interface SettingsRepository {
    val language: Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)

    val theme: Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)
}
