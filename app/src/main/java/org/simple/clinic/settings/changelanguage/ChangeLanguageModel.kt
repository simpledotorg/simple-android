package org.simple.clinic.settings.changelanguage

import org.simple.clinic.settings.Language

data class ChangeLanguageModel(
    val currentLanguage: Language?,
    val supportedLanguages: List<Language>
) {
  companion object {
    val FETCHING_LANGUAGES = ChangeLanguageModel(currentLanguage = null, supportedLanguages = emptyList())
  }

  fun withCurrentLanguage(currentLanguage: Language): ChangeLanguageModel {
    return copy(currentLanguage = currentLanguage)
  }

  fun withSupportedLanguages(supportedLanguages: List<Language>): ChangeLanguageModel {
    return copy(supportedLanguages = supportedLanguages)
  }
}
