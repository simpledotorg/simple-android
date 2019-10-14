package org.simple.clinic.settings

sealed class SettingsEvent

data class UserDetailsLoaded(val name: String, val phoneNumber: String): SettingsEvent()

data class CurrentLanguageLoaded(val language: Language): SettingsEvent()
