package victor.android.textify.speech

import android.content.Context
import android.os.Build
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Crea instancias de [SpeechRecognizer]. Se abstrae para poder simularla en tests. */
interface SpeechRecognizerFactory {
    fun isOnDeviceAvailable(): Boolean
    fun isStandardAvailable(): Boolean
    fun createOnDevice(): SpeechRecognizer
    fun createStandard(): SpeechRecognizer
}

class AndroidSpeechRecognizerFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechRecognizerFactory {

    override fun isOnDeviceAvailable(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    override fun isStandardAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    override fun createOnDevice(): SpeechRecognizer {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { "Requiere API 31+" }
        return SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
    }

    override fun createStandard(): SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
}
