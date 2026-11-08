package victor.android.textify.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import victor.android.textify.R
import victor.android.textify.ui.theme.TextifyTheme

/** Pantalla provisional; se reemplaza por la pantalla de grabación en la etapa 4. */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.home_placeholder),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TextifyTheme {
        HomeScreen()
    }
}
