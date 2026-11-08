package victor.android.textify.testutil

import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Se usa Robolectric para tener un `Dispatchers.Main` real (el main looper de Android) y así poder
 * comprobar que la regla lo restaura al terminar.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MainDispatcherRuleTest {

    private val description = Description.createTestDescription(javaClass, "dummy")

    private fun MainDispatcherRule.runWithRule(block: () -> Unit) {
        apply(object : Statement() {
            override fun evaluate() = block()
        }, description).evaluate()
    }

    @Test
    fun `durante el test Dispatchers Main usa el dispatcher de prueba`() {
        val rule = MainDispatcherRule(StandardTestDispatcher())
        var ran = false

        rule.runWithRule {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            scope.launch { ran = true }

            // Con StandardTestDispatcher nada corre hasta avanzar su scheduler.
            assertFalse(ran)
            rule.testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(ran)
            scope.cancel()
        }
    }

    @Test
    fun `al terminar el test Dispatchers Main se restaura`() {
        val rule = MainDispatcherRule(StandardTestDispatcher())
        rule.runWithRule { /* nada */ }

        var ran = false
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        scope.launch { ran = true }

        // El dispatcher de prueba ya no controla Dispatchers.Main...
        rule.testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(ran)

        // ...ahora lo ejecuta el main looper real de Android.
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(ran)
        scope.cancel()
    }

    @Test
    fun `el dispatcher por defecto es UnconfinedTestDispatcher y ejecuta de inmediato`() {
        val rule = MainDispatcherRule()
        var ran = false

        rule.runWithRule {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            scope.launch { ran = true }
            assertTrue(ran)
            scope.cancel()
        }
    }
}
