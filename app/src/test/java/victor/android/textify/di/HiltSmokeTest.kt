package victor.android.textify.di

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import victor.android.textify.MainActivity
import victor.android.textify.R
import javax.inject.Inject

/** Prueba de humo: el grafo de Hilt de prueba se construye y la actividad principal arranca. */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class HiltSmokeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var holder: TestDispatcherHolder

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    @DefaultDispatcher
    lateinit var defaultDispatcher: CoroutineDispatcher

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `el modulo de prueba reemplaza a los dispatchers reales`() {
        assertSame(holder.dispatcher, ioDispatcher)
        assertSame(holder.dispatcher, defaultDispatcher)
    }

    @Test
    fun `MainActivity arranca con Hilt y muestra el NavHost`() {
        val placeholder = composeRule.activity.getString(R.string.home_placeholder)
        composeRule.onNodeWithText(placeholder).assertExists()
    }
}
