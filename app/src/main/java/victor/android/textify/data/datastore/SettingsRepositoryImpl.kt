package victor.android.textify.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import victor.android.textify.domain.model.AppLanguage
import victor.android.textify.domain.model.AppTheme
import victor.android.textify.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val language: Flow<AppLanguage> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY]?.let { stored ->
            AppLanguage.entries.firstOrNull { it.name == stored }
        } ?: AppLanguage.ES_MX
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[LANGUAGE_KEY] = language.name }
    }

    override val theme: Flow<AppTheme> = dataStore.data.map { preferences ->
        preferences[THEME_KEY]?.let { stored ->
            AppTheme.entries.firstOrNull { it.name == stored }
        } ?: AppTheme.SYSTEM
    }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[THEME_KEY] = theme.name }
    }

    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val THEME_KEY = stringPreferencesKey("theme")
    }
}
