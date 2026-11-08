package victor.android.textify.ui.permission

/** Estado del permiso de micrófono (`RECORD_AUDIO`). */
sealed interface MicPermissionState {
    /** El permiso está concedido. */
    data object Granted : MicPermissionState

    /** El permiso no está concedido, pero se puede volver a pedir. */
    data object Denied : MicPermissionState

    /** El usuario lo denegó de forma permanente; hay que llevarlo a Ajustes. */
    data object PermanentlyDenied : MicPermissionState
}

/**
 * Decide el [MicPermissionState] a partir de las señales del sistema.
 *
 * Cuando el permiso no está concedido y el sistema tampoco pide mostrar una
 * explicación ([shouldShowRationale] en `false`), eso puede significar dos cosas
 * opuestas: que todavía no se pidió nunca, o que el usuario lo denegó para siempre.
 * [requestedBefore] distingue ambos casos.
 */
fun resolveMicPermissionState(
    granted: Boolean,
    shouldShowRationale: Boolean,
    requestedBefore: Boolean,
): MicPermissionState = when {
    granted -> MicPermissionState.Granted
    shouldShowRationale -> MicPermissionState.Denied
    requestedBefore -> MicPermissionState.PermanentlyDenied
    else -> MicPermissionState.Denied
}
