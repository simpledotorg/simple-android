package org.simple.clinic.newentry

import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent

sealed class PatientEntryEvent : UiEvent

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

object SaveClicked : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Save Clicked"
}

data class OngoingPatientEntryChanged(val entry: OngoingNewPatientEntry) : PatientEntryEvent() {
  override val analyticsName = "Create Patient Entry:Ongoing Patient Entry Changed"
}
