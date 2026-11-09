package victor.android.textify.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import victor.android.textify.R
import victor.android.textify.domain.model.AppLanguage
import victor.android.textify.domain.model.AppTheme
import victor.android.textify.ui.theme.TextifyTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        onLanguageSelected = viewModel::onLanguageSelected,
        onThemeSelected = viewModel::onThemeSelected,
        modifier = modifier,
    )
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.settings_language_section), style = MaterialTheme.typography.titleMedium)
        AppLanguage.entries.forEach { language ->
            SettingsRadioRow(
                label = stringResource(languageLabelRes(language)),
                selected = uiState.language == language,
                onClick = { onLanguageSelected(language) },
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(stringResource(R.string.settings_theme_section), style = MaterialTheme.typography.titleMedium)
        AppTheme.entries.forEach { theme ->
            SettingsRadioRow(
                label = stringResource(themeLabelRes(theme)),
                selected = uiState.theme == theme,
                onClick = { onThemeSelected(theme) },
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(stringResource(R.string.settings_privacy_title), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(R.string.settings_privacy_description),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SettingsRadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}

private fun languageLabelRes(language: AppLanguage): Int = when (language) {
    AppLanguage.ES_MX -> R.string.settings_language_es_mx
    AppLanguage.EN_US -> R.string.settings_language_en_us
}

private fun themeLabelRes(theme: AppTheme): Int = when (theme) {
    AppTheme.SYSTEM -> R.string.settings_theme_system
    AppTheme.LIGHT -> R.string.settings_theme_light
    AppTheme.DARK -> R.string.settings_theme_dark
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    TextifyTheme {
        SettingsScreen(SettingsUiState(), {}, {})
    }
}
