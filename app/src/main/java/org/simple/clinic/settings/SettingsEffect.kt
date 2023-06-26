package org.simple.clinic.settings

sealed class SettingsEffect

object LoadUserDetailsEffect : SettingsEffect()

object LoadCurrentLanguageEffect : SettingsEffect()

object LoadAppVersionEffect : SettingsEffect()

object CheckAppUpdateAvailable : SettingsEffect()

sealed class SettingsViewEffect : SettingsEffect()

object LogoutUser : SettingsEffect()

object OpenLanguageSelectionScreenEffect : SettingsViewEffect()

object ShowConfirmLogoutDialog : SettingsViewEffect()
