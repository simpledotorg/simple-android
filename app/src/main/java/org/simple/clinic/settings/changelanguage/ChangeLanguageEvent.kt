package org.simple.clinic.settings.changelanguage

import org.simple.clinic.settings.Language

sealed class ChangeLanguageEvent

data class CurrentSelectedLanguageLoadedEvent(val language: Language) : ChangeLanguageEvent()

data class SupportedLanguagesLoadedEvent(val languages: List<Language>) : ChangeLanguageEvent()

data class SelectLanguageEvent(val newLanguage: Language): ChangeLanguageEvent()

data class SelectedLanguageChangedEvent(val selectedLanguage: Language): ChangeLanguageEvent()
