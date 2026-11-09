package victor.android.textify.util

import android.content.Intent

/** Construye un [Intent] para compartir [text] como texto plano. Nunca comparte archivos. */
fun createShareTextIntent(text: String): Intent =
    Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
