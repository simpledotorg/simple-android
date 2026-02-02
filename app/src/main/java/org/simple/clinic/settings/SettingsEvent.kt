package org.simple.clinic.settings

import org.simple.clinic.patient.CompleteMedicalRecord
import org.simple.clinic.patient.medicalRecords.PushMedicalRecordsOnline
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

sealed class SettingsEvent : UiEvent

data class UserDetailsLoaded(val name: String, val phoneNumber: String) : SettingsEvent()

data class CurrentLanguageLoaded(val language: Language) : SettingsEvent()

data object ChangeLanguage : SettingsEvent() {
  override val analyticsName: String = "Settings:Change Language Clicked"
}

data class AppVersionLoaded(val appVersion: String) : SettingsEvent()

data class AppUpdateAvailabilityChecked(val isUpdateAvailable: Boolean) : SettingsEvent()

data class UserLogoutResult(val result: UserSession.LogoutResult) : SettingsEvent()

data object LogoutButtonClicked : SettingsEvent() {
  override val analyticsName: String = "Settings:Logout Button Clicked"
}

data object ConfirmLogoutButtonClicked : SettingsEvent() {
  override val analyticsName: String = "Settings:Confirm Logout Button Clicked"
}

data object BackClicked : SettingsEvent() {
  override val analyticsName: String = "Settings:Back Clicked"
}

data class DatabaseEncryptionStatusLoaded(val isDatabaseEncrypted: Boolean) : SettingsEvent()

data class MedicalRecordsFetched(
    val completeMedicalRecords: List<CompleteMedicalRecord>
) : SettingsEvent()

data class PushMedicalRecordsOnlineCompleted(
    val result: PushMedicalRecordsOnline.Result,
) : SettingsEvent()
