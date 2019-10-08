package org.simple.clinic.settings

sealed class Language

data class ProvidedLanguage(val displayName: String, val languageCode: String) : Language()

object SystemDefaultLanguage : Language()
