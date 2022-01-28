package org.simple.clinic.settings

sealed class SettingsEffect

object LoadUserDetailsEffect : SettingsEffect()

object LoadCurrentLanguageEffect : SettingsEffect()

data class LoadAppVersionEffect(val applicationId: String) : SettingsEffect()

object CheckAppUpdateAvailable : SettingsEffect()

sealed class SettingsViewEffect : SettingsEffect()

object OpenLanguageSelectionScreenEffect : SettingsViewEffect()
