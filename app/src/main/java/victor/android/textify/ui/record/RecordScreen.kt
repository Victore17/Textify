package victor.android.textify.ui.record

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import victor.android.textify.R
import victor.android.textify.speech.SpeechError
import victor.android.textify.ui.theme.TextifyTheme

@Composable
fun RecordScreen(
    modifier: Modifier = Modifier,
    viewModel: RecordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    RecordScreen(
        uiState = uiState,
        onRecordClick = viewModel::onRecordClick,
        onStopClick = viewModel::onStopClick,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

@Composable
private fun RecordScreen(
    uiState: RecordUiState,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (uiState) {
            is RecordUiState.Idle -> IdleContent(onRecordClick)
            is RecordUiState.Listening -> ListeningContent(uiState.partialText, onStopClick)
            is RecordUiState.Review -> ReviewContent(uiState.text, onRetry)
            is RecordUiState.Error -> ErrorContent(uiState.error, onRetry)
        }
    }
}

@Composable
private fun IdleContent(onRecordClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.record_idle_hint), style = MaterialTheme.typography.bodyLarge)
        MicButton(
            contentDescription = stringResource(R.string.record_mic_button_description),
            onClick = onRecordClick,
            icon = Icons.Filled.Mic,
        )
    }
}

@Composable
private fun ListeningContent(partialText: String, onStopClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "mic_pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mic_pulse_scale",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.record_listening_hint), style = MaterialTheme.typography.bodyLarge)
        MicButton(
            contentDescription = stringResource(R.string.record_stop_button_description),
            onClick = onStopClick,
            icon = Icons.Filled.Stop,
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
        )
        Text(
            text = partialText,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun ReviewContent(text: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.record_again_button)) }
    }
}

@Composable
private fun ErrorContent(error: SpeechError, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(errorMessageRes(error)),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.record_retry_button)) }
    }
}

@Composable
private fun MicButton(
    contentDescription: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(96.dp),
        colors = IconButtonDefaults.filledIconButtonColors(),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(48.dp))
    }
}

private fun errorMessageRes(error: SpeechError): Int = when (error) {
    SpeechError.NO_MATCH -> R.string.error_no_match
    SpeechError.NO_SPEECH -> R.string.error_no_speech
    SpeechError.NETWORK_UNAVAILABLE -> R.string.error_network_unavailable
    SpeechError.BUSY -> R.string.error_busy
    SpeechError.PERMISSION_DENIED -> R.string.error_permission_denied
    SpeechError.LANGUAGE_NOT_SUPPORTED -> R.string.error_language_not_supported
    SpeechError.OTHER -> R.string.error_other
}

@Preview(showBackground = true)
@Composable
private fun RecordScreenIdlePreview() {
    TextifyTheme { RecordScreen(RecordUiState.Idle, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun RecordScreenListeningPreview() {
    TextifyTheme { RecordScreen(RecordUiState.Listening("hola mundo"), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun RecordScreenReviewPreview() {
    TextifyTheme { RecordScreen(RecordUiState.Review("Texto transcrito final"), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun RecordScreenErrorPreview() {
    TextifyTheme { RecordScreen(RecordUiState.Error(SpeechError.NO_SPEECH), {}, {}, {}) }
}
