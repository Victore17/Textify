package victor.android.textify.speech

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * [SpeechToTextEngine] basado en [SpeechRecognizer].
 *
 * - Usa el reconocedor en el dispositivo cuando existe (API 31+). Si no existe, o si no tiene
 *   el idioma pedido, recurre al reconocedor estándar del sistema (que puede usar servidores).
 * - Pide resultados parciales para mostrar la transcripción en vivo.
 * - No graba ni guarda audio: no usa `MediaRecorder` ni `AudioRecord` y no pide el audio
 *   al reconocedor. Solo recibe texto.
 *
 * Debe usarse desde el hilo principal, como exige [SpeechRecognizer].
 */
class AndroidSpeechToTextEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val factory: SpeechRecognizerFactory,
) : SpeechToTextEngine {

    private val _state = MutableStateFlow<SpeechState>(SpeechState.Idle)
    override val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var recognizer: SpeechRecognizer? = null

    /** Listener de la sesión activa; los callbacks de sesiones anteriores se descartan. */
    private var activeListener: RecognitionStateListener? = null

    override fun start(languageTag: String) {
        destroyRecognizer()
        val onDevice = factory.isOnDeviceAvailable()
        if (!onDevice && !factory.isStandardAvailable()) {
            _state.value = SpeechState.Error(SpeechError.OTHER)
            return
        }
        _state.value = SpeechState.Listening
        startSession(languageTag, onDevice)
    }

    override fun stop() {
        recognizer?.stopListening()
    }

    override fun release() {
        destroyRecognizer()
        _state.value = SpeechState.Idle
    }

    private fun startSession(languageTag: String, onDevice: Boolean) {
        val newRecognizer = if (onDevice) factory.createOnDevice() else factory.createStandard()
        lateinit var listener: RecognitionStateListener
        listener = RecognitionStateListener { newState ->
            if (activeListener === listener) handleState(newState, languageTag, onDevice)
        }
        activeListener = listener
        recognizer = newRecognizer
        newRecognizer.setRecognitionListener(listener)
        newRecognizer.startListening(buildRecognizerIntent(context.packageName, languageTag))
    }

    private fun handleState(newState: SpeechState, languageTag: String, onDevice: Boolean) {
        val current = _state.value
        when {
            // Algunos reconocedores envían un error espurio después del resultado final.
            current is SpeechState.Result && newState is SpeechState.Error -> Unit

            // El reconocedor en el dispositivo no tiene el idioma: respaldo al estándar.
            onDevice &&
                newState == SpeechState.Error(SpeechError.LANGUAGE_NOT_SUPPORTED) &&
                factory.isStandardAvailable() -> {
                destroyRecognizer()
                startSession(languageTag, onDevice = false)
            }

            else -> _state.value = newState
        }
    }

    private fun destroyRecognizer() {
        activeListener = null
        recognizer?.let {
            it.cancel()
            it.destroy()
        }
        recognizer = null
    }
}

/**
 * Construye el [Intent] de reconocimiento: texto libre, idioma [languageTag] y resultados
 * parciales. No incluye ningún extra para obtener o guardar el audio.
 */
fun buildRecognizerIntent(callingPackage: String, languageTag: String): Intent =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULTS)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, callingPackage)
    }

private const val MAX_RESULTS = 5
