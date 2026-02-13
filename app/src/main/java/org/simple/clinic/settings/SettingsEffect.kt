package org.simple.clinic.settings

import org.simple.clinic.patient.CompleteMedicalRecord

sealed class SettingsEffect

data object LoadUserDetailsEffect : SettingsEffect()

data object LoadCurrentLanguageEffect : SettingsEffect()

data object LoadAppVersionEffect : SettingsEffect()

data object CheckAppUpdateAvailable : SettingsEffect()

data object LogoutUser : SettingsEffect()

data object LoadDatabaseEncryptionStatus : SettingsEffect()

sealed class SettingsViewEffect : SettingsEffect()

data object OpenLanguageSelectionScreenEffect : SettingsViewEffect()

data object ShowConfirmLogoutDialog : SettingsViewEffect()

data object RestartApp : SettingsViewEffect()

data object GoBack : SettingsViewEffect()

data object FetchCompleteMedicalRecords : SettingsEffect()

data class PushCompleteMedicalRecordsOnline(
    val medicalRecords: List<CompleteMedicalRecord>
) : SettingsEffect()

