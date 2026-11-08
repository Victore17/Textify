package victor.android.textify.ui.permission

import org.junit.Assert.assertEquals
import org.junit.Test

class MicPermissionStateHolderTest {

    @Test
    fun `estado inicial concedido es Granted`() {
        val holder = MicPermissionStateHolder(initialGranted = true)
        assertEquals(MicPermissionState.Granted, holder.state)
    }

    @Test
    fun `estado inicial no concedido es Denied`() {
        val holder = MicPermissionStateHolder(initialGranted = false)
        assertEquals(MicPermissionState.Denied, holder.state)
    }

    @Test
    fun `primera respuesta denegada con rationale queda en Denied`() {
        val holder = MicPermissionStateHolder(initialGranted = false)

        holder.onPermissionResult(granted = false, shouldShowRationale = true)

        assertEquals(MicPermissionState.Denied, holder.state)
    }

    @Test
    fun `primera respuesta denegada sin rationale queda PermanentlyDenied`() {
        val holder = MicPermissionStateHolder(initialGranted = false)

        holder.onPermissionResult(granted = false, shouldShowRationale = false)

        assertEquals(MicPermissionState.PermanentlyDenied, holder.state)
    }

    @Test
    fun `respuesta concedida despues de pedirlo queda en Granted`() {
        val holder = MicPermissionStateHolder(initialGranted = false)

        holder.onPermissionResult(granted = true, shouldShowRationale = false)

        assertEquals(MicPermissionState.Granted, holder.state)
    }

    @Test
    fun `secuencia completa denegado luego permanente luego concedido`() {
        val holder = MicPermissionStateHolder(initialGranted = false)

        holder.onPermissionResult(granted = false, shouldShowRationale = true)
        assertEquals(MicPermissionState.Denied, holder.state)

        holder.onPermissionResult(granted = false, shouldShowRationale = false)
        assertEquals(MicPermissionState.PermanentlyDenied, holder.state)

        holder.onPermissionResult(granted = true, shouldShowRationale = false)
        assertEquals(MicPermissionState.Granted, holder.state)
    }
}
