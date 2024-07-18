package org.simple.clinic.settings.changelanguage

import org.simple.clinic.settings.Language

sealed class ChangeLanguageEffect

data object LoadCurrentLanguageEffect : ChangeLanguageEffect()

data object LoadSupportedLanguagesEffect : ChangeLanguageEffect()

data class UpdateCurrentLanguageEffect(val newLanguage: Language) : ChangeLanguageEffect()

data object GoBack : ChangeLanguageEffect()

data object RestartActivity : ChangeLanguageEffect()

data object TriggerSync : ChangeLanguageEffect()
