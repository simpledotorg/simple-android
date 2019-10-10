package org.simple.clinic.settings.changelanguage

import org.simple.clinic.settings.Language

data class ChangeLanguageModel(
    val currentLanguage: Language?,
    val userSelectedLanguage: Language?,
    val supportedLanguages: List<Language>
) {
  val haveLanguagesBeenFetched: Boolean
    get() = currentLanguage != null && supportedLanguages.isNotEmpty()

  companion object {
    val FETCHING_LANGUAGES = ChangeLanguageModel(currentLanguage = null, userSelectedLanguage = null, supportedLanguages = emptyList())
  }

  fun withCurrentLanguage(currentLanguage: Language): ChangeLanguageModel {
    return copy(currentLanguage = currentLanguage)
        .coerceUserSelectedLanguage()
  }

  fun withSupportedLanguages(supportedLanguages: List<Language>): ChangeLanguageModel {
    return copy(supportedLanguages = supportedLanguages)
        .coerceUserSelectedLanguage()
  }

  fun withUserSelectedLanguage(userSelectedLanguage: Language): ChangeLanguageModel {
    return copy(userSelectedLanguage = userSelectedLanguage)
  }

  private fun coerceUserSelectedLanguage(): ChangeLanguageModel {
    val isCurrentLanguageInSupportedLanguages = haveLanguagesBeenFetched && currentLanguage in supportedLanguages

    return if (isCurrentLanguageInSupportedLanguages) copy(userSelectedLanguage = currentLanguage) else this
  }
}
