package victor.android.textify.ui.permission

import org.junit.Assert.assertEquals
import org.junit.Test

class MicPermissionStateTest {

    @Test
    fun `permiso concedido siempre es Granted`() {
        assertEquals(
            MicPermissionState.Granted,
            resolveMicPermissionState(granted = true, shouldShowRationale = false, requestedBefore = false),
        )
        assertEquals(
            MicPermissionState.Granted,
            resolveMicPermissionState(granted = true, shouldShowRationale = true, requestedBefore = true),
        )
    }

    @Test
    fun `no concedido y con rationale es Denied sin importar si se pidio antes`() {
        assertEquals(
            MicPermissionState.Denied,
            resolveMicPermissionState(granted = false, shouldShowRationale = true, requestedBefore = false),
        )
        assertEquals(
            MicPermissionState.Denied,
            resolveMicPermissionState(granted = false, shouldShowRationale = true, requestedBefore = true),
        )
    }

    @Test
    fun `no concedido sin rationale y nunca pedido antes es Denied (primera vez)`() {
        assertEquals(
            MicPermissionState.Denied,
            resolveMicPermissionState(granted = false, shouldShowRationale = false, requestedBefore = false),
        )
    }

    @Test
    fun `no concedido sin rationale y ya pedido antes es PermanentlyDenied`() {
        assertEquals(
            MicPermissionState.PermanentlyDenied,
            resolveMicPermissionState(granted = false, shouldShowRationale = false, requestedBefore = true),
        )
    }
}
