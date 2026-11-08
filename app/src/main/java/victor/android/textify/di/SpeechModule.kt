package victor.android.textify.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import victor.android.textify.speech.AndroidSpeechRecognizerFactory
import victor.android.textify.speech.AndroidSpeechToTextEngine
import victor.android.textify.speech.SpeechRecognizerFactory
import victor.android.textify.speech.SpeechToTextEngine

/**
 * Enlaza el motor de voz. Sin scope: cada ViewModel recibe su propia instancia
 * y es responsable de llamar a `release()`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechModule {

    @Binds
    abstract fun bindSpeechToTextEngine(impl: AndroidSpeechToTextEngine): SpeechToTextEngine

    @Binds
    abstract fun bindSpeechRecognizerFactory(
        impl: AndroidSpeechRecognizerFactory,
    ): SpeechRecognizerFactory
}
