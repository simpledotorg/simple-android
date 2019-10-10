package org.simple.clinic.settings.changelanguage

import org.simple.clinic.settings.Language

sealed class ChangeLanguageEffect

object LoadCurrentSelectedLanguageEffect : ChangeLanguageEffect()

object LoadSupportedLanguagesEffect : ChangeLanguageEffect()

data class UpdateSelectedLanguageEffect(val newLanguage: Language) : ChangeLanguageEffect()
