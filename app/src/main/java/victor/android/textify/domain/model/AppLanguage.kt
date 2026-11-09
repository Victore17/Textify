package victor.android.textify.domain.model

/** Idioma de dictado. [tag] sigue BCP 47, el formato que espera el reconocedor de voz. */
enum class AppLanguage(val tag: String) {
    ES_MX("es-MX"),
    EN_US("en-US"),
}
