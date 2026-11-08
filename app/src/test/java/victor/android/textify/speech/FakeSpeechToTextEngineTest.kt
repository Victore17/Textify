package victor.android.textify.speech

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.android.textify.testutil.FakeSpeechToTextEngine

class FakeSpeechToTextEngineTest {

    @Test
    fun `emite la secuencia Idle, Listening, Partial, Result`() = runTest {
        val engine = FakeSpeechToTextEngine()

        engine.state.test {
            assertEquals(SpeechState.Idle, awaitItem())
            engine.start("es-MX")
            assertEquals(SpeechState.Listening, awaitItem())
            engine.emitPartial("hola")
            assertEquals(SpeechState.Partial("hola"), awaitItem())
            engine.emitResult("hola mundo")
            assertEquals(SpeechState.Result("hola mundo"), awaitItem())
        }
        assertEquals(listOf("es-MX"), engine.startedLanguages)
    }

    @Test
    fun `release vuelve a Idle y queda registrado`() {
        val engine = FakeSpeechToTextEngine()
        engine.start("en-US")

        engine.release()

        assertTrue(engine.released)
        assertEquals(SpeechState.Idle, engine.state.value)
    }
}
