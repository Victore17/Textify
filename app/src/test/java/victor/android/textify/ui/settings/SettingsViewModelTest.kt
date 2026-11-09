package victor.android.textify.ui.settings

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import victor.android.textify.domain.model.AppLanguage
import victor.android.textify.domain.model.AppTheme
import victor.android.textify.testutil.FakeSettingsRepository
import victor.android.textify.testutil.MainDispatcherRule

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `el estado inicial refleja los ajustes guardados`() {
        val repository = FakeSettingsRepository(AppLanguage.EN_US, AppTheme.DARK)
        val viewModel = SettingsViewModel(repository)

        assertEquals(SettingsUiState(AppLanguage.EN_US, AppTheme.DARK), viewModel.uiState.value)
    }

    @Test
    fun `cambiar el idioma actualiza el repositorio y el estado`() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)

        viewModel.onLanguageSelected(AppLanguage.EN_US)

        assertEquals(AppLanguage.EN_US, viewModel.uiState.value.language)
        assertEquals(AppLanguage.EN_US, repository.language.value)
    }

    @Test
    fun `cambiar el tema actualiza el repositorio y el estado`() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)

        viewModel.onThemeSelected(AppTheme.LIGHT)

        assertEquals(AppTheme.LIGHT, viewModel.uiState.value.theme)
        assertEquals(AppTheme.LIGHT, repository.theme.value)
    }
}
