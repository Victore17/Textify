package victor.android.textify.util

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShareIntentTest {

    @Test
    fun `el intent tiene accion ACTION_SEND tipo texto plano y el texto como extra`() {
        val intent = createShareTextIntent("hola mundo")

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals("hola mundo", intent.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun `no comparte ningun archivo`() {
        val intent = createShareTextIntent("hola")

        assertNull(intent.getParcelableExtra<android.os.Parcelable>(Intent.EXTRA_STREAM))
    }
}
