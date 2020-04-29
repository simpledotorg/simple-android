package org.simple.clinic.settings.changelanguage

import org.simple.clinic.settings.Language
import org.simple.clinic.settings.ProvidedLanguage
import org.simple.clinic.widgets.UiEvent

sealed class ChangeLanguageEvent : UiEvent

data class CurrentLanguageLoadedEvent(val language: Language) : ChangeLanguageEvent()

data class SupportedLanguagesLoadedEvent(val languages: List<Language>) : ChangeLanguageEvent()

data class SelectLanguageEvent(val newLanguage: Language) : ChangeLanguageEvent() {
  override val analyticsName: String
    get() {
      val languageCode = if (newLanguage is ProvidedLanguage) newLanguage.languageCode else "default"

      return "Change Language:Select Language:$languageCode"
    }
}

object CurrentLanguageChangedEvent : ChangeLanguageEvent()

object SaveCurrentLanguageEvent : ChangeLanguageEvent() {
  override val analyticsName: String
    get() = "Change Language:Done Clicked"
}
