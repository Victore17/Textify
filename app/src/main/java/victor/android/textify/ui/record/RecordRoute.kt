package victor.android.textify.ui.record

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import victor.android.textify.ui.permission.MicPermissionRequest

/** Pide el permiso de micrófono antes de mostrar la pantalla de grabación. */
@Composable
fun RecordRoute(modifier: Modifier = Modifier) {
    var permissionGranted by remember { mutableStateOf(false) }
    if (permissionGranted) {
        RecordScreen(modifier = modifier)
    } else {
        MicPermissionRequest(
            onPermissionGranted = { permissionGranted = true },
            modifier = modifier,
        )
    }
}
