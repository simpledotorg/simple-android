package org.simple.clinic.settings.changelanguage

import org.simple.clinic.settings.Language
import org.simple.clinic.widgets.UiEvent

sealed class ChangeLanguageEvent: UiEvent

data class CurrentLanguageLoadedEvent(val language: Language) : ChangeLanguageEvent()

data class SupportedLanguagesLoadedEvent(val languages: List<Language>) : ChangeLanguageEvent()

data class SelectLanguageEvent(val newLanguage: Language) : ChangeLanguageEvent()

object CurrentLanguageChangedEvent : ChangeLanguageEvent()

object SaveCurrentLanguageEvent : ChangeLanguageEvent()
