package org.simple.clinic.settings.changelanguage

import org.simple.clinic.settings.Language

sealed class ChangeLanguageEffect

object LoadCurrentLanguageEffect : ChangeLanguageEffect()

object LoadSupportedLanguagesEffect : ChangeLanguageEffect()

data class UpdateCurrentLanguageEffect(val newLanguage: Language) : ChangeLanguageEffect()

object GoBack : ChangeLanguageEffect()
