package org.simple.clinic.settings

sealed class SettingsEffect

object LoadUserDetailsEffect : SettingsEffect()

object LoadCurrentLanguageEffect : SettingsEffect()

object LoadAppVersionEffect : SettingsEffect()

object CheckAppUpdateAvailable : SettingsEffect()

object LogoutUser : SettingsEffect()

object LoadDatabaseEncryptionStatus : SettingsEffect()

sealed class SettingsViewEffect : SettingsEffect()

object OpenLanguageSelectionScreenEffect : SettingsViewEffect()

object ShowConfirmLogoutDialog : SettingsViewEffect()

object RestartApp : SettingsViewEffect()

object GoBack : SettingsViewEffect()
