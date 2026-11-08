package victor.android.textify.ui.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import victor.android.textify.R

/**
 * Pide el permiso de micrófono: muestra una explicación previa, el botón para
 * pedirlo y, si quedó denegado para siempre, un botón para ir a Ajustes.
 * No dibuja nada cuando el permiso ya está concedido.
 */
@Composable
fun MicPermissionRequest(
    onPermissionGranted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalActivity.current
    val stateHolder = remember {
        MicPermissionStateHolder(
            initialGranted = activity != null &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(
            Manifest.permission.RECORD_AUDIO,
        ) ?: false
        stateHolder.onPermissionResult(granted, shouldShowRationale)
    }

    LaunchedEffect(stateHolder.state) {
        if (stateHolder.state is MicPermissionState.Granted) onPermissionGranted()
    }

    when (stateHolder.state) {
        is MicPermissionState.Granted -> Unit

        is MicPermissionState.Denied -> MicPermissionExplanation(
            text = stringResource(R.string.mic_permission_rationale),
            buttonText = stringResource(R.string.mic_permission_request_button),
            onButtonClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) },
            modifier = modifier,
        )

        is MicPermissionState.PermanentlyDenied -> {
            val context = activity
            MicPermissionExplanation(
                text = stringResource(R.string.mic_permission_denied_permanently),
                buttonText = stringResource(R.string.mic_permission_open_settings_button),
                onButtonClick = {
                    if (context != null) {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            },
                        )
                    }
                },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun MicPermissionExplanation(
    text: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onButtonClick) { Text(buttonText) }
    }
}
