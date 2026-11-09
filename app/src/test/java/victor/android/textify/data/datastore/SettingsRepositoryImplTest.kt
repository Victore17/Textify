package victor.android.textify.data.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import victor.android.textify.domain.model.AppLanguage
import victor.android.textify.domain.model.AppTheme

class SettingsRepositoryImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun repository(): SettingsRepositoryImpl {
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { temporaryFolder.newFile("settings.preferences_pb") },
        )
        return SettingsRepositoryImpl(dataStore)
    }

    @Test
    fun `el idioma por defecto es es-MX`() = runTest {
        assertEquals(AppLanguage.ES_MX, repository().language.first())
    }

    @Test
    fun `el tema por defecto es SYSTEM`() = runTest {
        assertEquals(AppTheme.SYSTEM, repository().theme.first())
    }

    @Test
    fun `guardar y leer el idioma`() = runTest {
        val repo = repository()

        repo.setLanguage(AppLanguage.EN_US)

        assertEquals(AppLanguage.EN_US, repo.language.first())
    }

    @Test
    fun `guardar y leer el tema`() = runTest {
        val repo = repository()

        repo.setTheme(AppTheme.DARK)

        assertEquals(AppTheme.DARK, repo.theme.first())
    }
}
