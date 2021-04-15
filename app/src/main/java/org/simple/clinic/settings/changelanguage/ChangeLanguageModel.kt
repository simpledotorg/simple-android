package org.simple.clinic.settings.changelanguage

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.settings.Language

@Parcelize
data class ChangeLanguageModel(
    val currentLanguage: Language?,
    val userSelectedLanguage: Language?,
    val supportedLanguages: List<Language>,
    val manuallyRestarted: Boolean
) : Parcelable {
  val haveLanguagesBeenFetched: Boolean
    get() = currentLanguage != null && supportedLanguages.isNotEmpty()

  companion object {
    val FETCHING_LANGUAGES = ChangeLanguageModel(
        currentLanguage = null,
        userSelectedLanguage = null,
        supportedLanguages = emptyList(),
        manuallyRestarted = false
    )
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

  fun restarted(): ChangeLanguageModel {
    return copy(manuallyRestarted = true)
  }
}
