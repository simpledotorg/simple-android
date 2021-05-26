package org.simple.clinic.newentry

import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.ReminderConsent
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent

sealed class PatientEntryEvent : UiEvent

data class OngoingEntryFetched(val patientEntry: OngoingNewPatientEntry) : PatientEntryEvent()

data class FullNameChanged(val fullName: String) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Full Name Text Changed"
}

data class PhoneNumberChanged(val phoneNumber: String) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Phone Number Text Changed"
}

data class DateOfBirthChanged(val dateOfBirth: String) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:DOB Text Changed"
}

data class DateOfBirthFocusChanged(val hasFocus: Boolean) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Focused On DOB Text Field"
}

data class AgeChanged(val age: String) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Age Text Changed"
}

data class GenderChanged(val gender: Optional<Gender>) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Changed Gender"
}

data class ColonyOrVillageChanged(val colonyOrVillage: String) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Colony or Village Text Changed"
}

data class DistrictChanged(val district: String) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:District Text Changed"
}

data class StateChanged(val state: String) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:State Text Changed"
}

data class StreetAddressChanged(val streetAddress: String) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Street Address Changed"
}

data class ZoneChanged(val zone: String) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Zone Changed"
}

object SaveClicked : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Save Clicked"
}

object PatientEntrySaved : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Patient Entry Saved"
}

data class ReminderConsentChanged(val reminderConsent: ReminderConsent) : PatientEntryEvent() {
  override val analyticsName: String = "Create Patient Entry:Reminder Consent Changed"
}

data class AlternativeIdChanged(val identifier: Identifier) : PatientEntryEvent()

data class InputFieldsLoaded(val inputFields: InputFields) : PatientEntryEvent()

data class ColonyOrVillagesFetched(val colonyOrVillages: List<String>) : PatientEntryEvent()
