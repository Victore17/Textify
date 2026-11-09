package victor.android.textify.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidSpeechToTextEngineTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val factory = FakeRecognizerFactory()
    private val engine = AndroidSpeechToTextEngine(context, factory)

    @Test
    fun `estado inicial es Idle`() {
        assertEquals(SpeechState.Idle, engine.state.value)
    }

    @Test
    fun `start usa el reconocedor en el dispositivo si esta disponible`() {
        engine.start("es-MX")

        assertEquals(listOf("onDevice"), factory.created.map { it.kind })
        assertEquals(SpeechState.Listening, engine.state.value)
        verify { factory.created.single().recognizer.startListening(any()) }
    }

    @Test
    fun `start usa el reconocedor estandar si no hay uno en el dispositivo`() {
        factory.onDeviceAvailable = false

        engine.start("es-MX")

        assertEquals(listOf("standard"), factory.created.map { it.kind })
    }

    @Test
    fun `sin ningun reconocedor disponible pasa a Error`() {
        factory.onDeviceAvailable = false
        factory.standardAvailable = false

        engine.start("es-MX")

        assertEquals(SpeechState.Error(SpeechError.OTHER), engine.state.value)
        assertTrue(factory.created.isEmpty())
    }

    @Test
    fun `el intent pide el idioma, texto libre y resultados parciales`() {
        engine.start("en-US")

        val intent = factory.created.single().intent.captured
        assertIntent(intent, "en-US")
    }

    @Test
    fun `los resultados parciales se muestran mientras se escucha`() {
        engine.start("es-MX")
        val listener = factory.created.single().listener.captured

        listener.onPartialResults(results("hola"))

        assertEquals(SpeechState.Partial("hola"), engine.state.value)
    }

    @Test
    fun `un resultado final sin haber pedido detener no finaliza y reinicia la escucha`() {
        engine.start("es-MX")
        val first = factory.created.single()

        first.listener.captured.onResults(results("hola"))

        assertTrue(engine.state.value !is SpeechState.Result)
        assertEquals(2, factory.created.size)
        verify { first.recognizer.destroy() }
        assertIntent(factory.created[1].intent.captured, "es-MX")
    }

    @Test
    fun `el texto de sub-sesiones anteriores se conserva al reiniciar automaticamente`() {
        engine.start("es-MX")
        factory.created.single().listener.captured.onResults(results("hola"))

        factory.created[1].listener.captured.onPartialResults(results("mundo"))

        assertEquals(SpeechState.Partial("hola mundo"), engine.state.value)
    }

    @Test
    fun `despues de pedir detener, el resultado final entrega todo lo reconocido`() {
        engine.start("es-MX")
        factory.created.single().listener.captured.onResults(results("hola"))

        engine.stop()
        factory.created[1].listener.captured.onResults(results("mundo"))

        assertEquals(SpeechState.Result("hola mundo"), engine.state.value)
    }

    @Test
    fun `un error de silencio sin haber pedido detener reinicia la escucha en vez de mostrar error`() {
        engine.start("es-MX")
        val first = factory.created.single()

        first.listener.captured.onError(SpeechRecognizer.ERROR_SPEECH_TIMEOUT)

        assertTrue(engine.state.value !is SpeechState.Error)
        assertEquals(2, factory.created.size)
        verify { first.recognizer.destroy() }
    }

    @Test
    fun `un error de silencio despues de pedir detener entrega lo ya reconocido`() {
        engine.start("es-MX")
        factory.created.single().listener.captured.onResults(results("hola"))

        engine.stop()
        factory.created[1].listener.captured.onError(SpeechRecognizer.ERROR_NO_MATCH)

        assertEquals(SpeechState.Result("hola"), engine.state.value)
    }

    @Test
    fun `un error de silencio despues de pedir detener sin texto previo se reporta`() {
        engine.start("es-MX")

        engine.stop()
        factory.created.single().listener.captured.onError(SpeechRecognizer.ERROR_SPEECH_TIMEOUT)

        assertEquals(SpeechState.Error(SpeechError.NO_SPEECH), engine.state.value)
    }

    @Test
    fun `un error real como falta de red se reporta sin reiniciar`() {
        engine.start("es-MX")

        factory.created.single().listener.captured.onError(SpeechRecognizer.ERROR_NETWORK)

        assertEquals(SpeechState.Error(SpeechError.NETWORK_UNAVAILABLE), engine.state.value)
        assertEquals(1, factory.created.size)
    }

    @Test
    fun `un error despues del resultado final se ignora`() {
        engine.start("es-MX")
        val listener = factory.created.single().listener.captured
        engine.stop()

        listener.onResults(results("hola"))
        listener.onError(SpeechRecognizer.ERROR_CLIENT)

        assertEquals(SpeechState.Result("hola"), engine.state.value)
    }

    @Test
    fun `si el reconocedor en el dispositivo no tiene el idioma se usa el estandar`() {
        engine.start("es-MX")
        val onDevice = factory.created.single()

        onDevice.listener.captured.onError(ERROR_LANGUAGE_UNAVAILABLE)

        assertEquals(listOf("onDevice", "standard"), factory.created.map { it.kind })
        verify { onDevice.recognizer.destroy() }
        assertEquals(SpeechState.Listening, engine.state.value)
        assertIntent(factory.created[1].intent.captured, "es-MX")
    }

    @Test
    fun `si el estandar tampoco tiene el idioma se reporta el error`() {
        factory.onDeviceAvailable = false
        engine.start("xx-XX")

        factory.created.single().listener.captured.onError(ERROR_LANGUAGE_NOT_SUPPORTED)

        assertEquals(SpeechState.Error(SpeechError.LANGUAGE_NOT_SUPPORTED), engine.state.value)
        assertEquals(1, factory.created.size)
    }

    @Test
    fun `stop pide al reconocedor que deje de escuchar`() {
        engine.start("es-MX")

        engine.stop()

        verify { factory.created.single().recognizer.stopListening() }
    }

    @Test
    fun `release destruye el reconocedor y vuelve a Idle`() {
        engine.start("es-MX")

        engine.release()

        verify { factory.created.single().recognizer.destroy() }
        assertEquals(SpeechState.Idle, engine.state.value)
    }

    @Test
    fun `los callbacks de una sesion anterior se descartan`() {
        engine.start("es-MX")
        val oldListener = factory.created.single().listener.captured
        engine.start("es-MX")

        oldListener.onResults(results("viejo"))

        verify { factory.created[0].recognizer.destroy() }
        assertEquals(SpeechState.Listening, engine.state.value)
    }

    private fun assertIntent(intent: Intent, language: String) {
        assertEquals(RecognizerIntent.ACTION_RECOGNIZE_SPEECH, intent.action)
        assertEquals(language, intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE))
        assertEquals(
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL),
        )
        assertTrue(intent.getBooleanExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false))
        assertEquals(context.packageName, intent.getStringExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE))
        // Ningún extra para obtener o guardar el audio.
        val extras = intent.extras!!.keySet()
        assertFalse(extras.any { it.contains("AUDIO", ignoreCase = true) })
    }

    private fun results(vararg candidates: String) = Bundle().apply {
        putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf(*candidates))
    }

    private class Created(val kind: String) {
        val listener = slot<RecognitionListener>()
        val intent = slot<Intent>()
        val recognizer = mockk<SpeechRecognizer>(relaxed = true).also {
            every { it.setRecognitionListener(capture(listener)) } returns Unit
            every { it.startListening(capture(intent)) } returns Unit
        }
    }

    private class FakeRecognizerFactory : SpeechRecognizerFactory {
        var onDeviceAvailable = true
        var standardAvailable = true
        val created = mutableListOf<Created>()

        override fun isOnDeviceAvailable() = onDeviceAvailable
        override fun isStandardAvailable() = standardAvailable
        override fun createOnDevice() = Created("onDevice").also { created += it }.recognizer
        override fun createStandard() = Created("standard").also { created += it }.recognizer
    }
}
