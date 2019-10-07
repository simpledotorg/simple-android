package org.simple.clinic.settings.changelanguage

sealed class ChangeLanguageEffect

object LoadCurrentSelectedLanguageEffect: ChangeLanguageEffect()

object LoadSupportedLanguagesEffect: ChangeLanguageEffect()
