package org.simple.clinic.settings

sealed class SettingsEffect

object LoadUserDetailsEffect : SettingsEffect()

object LoadCurrentLanguageEffect : SettingsEffect()

object OpenLanguageSelectionScreenEffect : SettingsEffect()

data class LoadAppVersionEffect(val applicationId: String) : SettingsEffect()

object CheckAppUpdateAvailable : SettingsEffect()
