package victor.android.textify.speech

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Regla de privacidad: el audio nunca se guarda. Revisa el código de producción y el manifest
 * para asegurar que no aparecen APIs de grabación ni permisos de almacenamiento.
 */
class NoAudioStorageTest {

    private val forbiddenInCode = listOf(
        "MediaRecorder",
        "AudioRecord",
        "EXTRA_AUDIO_SOURCE",
        "EXTRA_AUDIO_INJECT_SOURCE",
        "EXTRA_SEGMENTED_SESSION",
        "android.speech.extra.GET_AUDIO",
        "FileOutputStream",
    )

    private val forbiddenInManifest = listOf(
        "WRITE_EXTERNAL_STORAGE",
        "MANAGE_EXTERNAL_STORAGE",
        "READ_MEDIA_AUDIO",
    )

    @Test
    fun `el codigo de produccion no usa APIs de grabacion ni escribe archivos de audio`() {
        val sources = File("src/main/java").walkTopDown().filter { it.extension == "kt" }.toList()
        assertTrue("No se encontró el código fuente", sources.isNotEmpty())

        val violations = sources.flatMap { file ->
            val code = file.readText().withoutComments()
            forbiddenInCode.filter { it in code }.map { "${file.name}: $it" }
        }
        assertTrue("Uso prohibido: $violations", violations.isEmpty())
    }

    @Test
    fun `el manifest no pide permisos de almacenamiento`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        val violations = forbiddenInManifest.filter { it in manifest }
        assertTrue("Permisos prohibidos: $violations", violations.isEmpty())
    }

    /** Quita comentarios para que la documentación pueda mencionar las APIs prohibidas. */
    private fun String.withoutComments(): String =
        replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("//.*"), "")
}
