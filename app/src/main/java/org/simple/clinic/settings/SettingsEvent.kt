package org.simple.clinic.settings

import org.simple.clinic.widgets.UiEvent

sealed class SettingsEvent : UiEvent

data class UserDetailsLoaded(val name: String, val phoneNumber: String) : SettingsEvent()

data class CurrentLanguageLoaded(val language: Language) : SettingsEvent()

object ChangeLanguage : SettingsEvent() {
  override val analyticsName: String = "Settings:Change Language Clicked"
}

data class AppVersionLoaded(val appVersion: String) : SettingsEvent()

data class AppUpdateAvailabilityChecked(val isUpdateAvailable: Boolean) : SettingsEvent()
