package victor.android.textify.ui.permission

import androidx.compose.runtime.mutableStateOf

/**
 * Mantiene el [MicPermissionState] y aplica las transiciones al recibir cada
 * respuesta del sistema de permisos.
 */
class MicPermissionStateHolder(
    initialGranted: Boolean,
    initialShouldShowRationale: Boolean = false,
) {
    private var requestedBefore = false

    private val stateHolder = mutableStateOf(
        resolveMicPermissionState(
            granted = initialGranted,
            shouldShowRationale = initialShouldShowRationale,
            requestedBefore = requestedBefore,
        ),
    )

    val state: MicPermissionState get() = stateHolder.value

    /** Se llama con la respuesta del sistema tras pedir el permiso. */
    fun onPermissionResult(granted: Boolean, shouldShowRationale: Boolean) {
        requestedBefore = true
        stateHolder.value = resolveMicPermissionState(granted, shouldShowRationale, requestedBefore)
    }
}
