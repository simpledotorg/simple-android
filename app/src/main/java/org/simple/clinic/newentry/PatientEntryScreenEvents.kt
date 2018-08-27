package org.simple.clinic.newentry

import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent

data class PatientFullNameTextChanged(val fullName: String) : UiEvent {
  override val analyticsName = "Create Patient Entry:Full Name Text Changed"
}

data class PatientPhoneNumberTextChanged(val phoneNumber: String) : UiEvent {
  override val analyticsName = "Create Patient Entry:Phone Number Text Changed"
}

data class PatientNoPhoneNumberToggled(val noneSelected: Boolean) : UiEvent {
  override val analyticsName = "Create Patient Entry:No Phone Number Toggled"
}

data class PatientNoColonyOrVillageToggled(val noneSelected: Boolean) : UiEvent {
  override val analyticsName = "Create Patient Entry:No Colony Or Village Toggled"
}

data class PatientDateOfBirthTextChanged(val dateOfBirth: String) : UiEvent {
  override val analyticsName = "Create Patient Entry:DOB Text Changed"
}

data class PatientDateOfBirthFocusChanged(val hasFocus: Boolean) : UiEvent {
  override val analyticsName = "Create Patient Entry:Focused On DOB Text Field"
}

data class PatientAgeTextChanged(val age: String) : UiEvent {
  override val analyticsName = "Create Patient Entry:Age Text Changed"
}

data class PatientGenderChanged(val gender: Optional<Gender>) : UiEvent {
  override val analyticsName = "Create Patient Entry:Changed Gender"
}

data class PatientColonyOrVillageTextChanged(val colonyOrVillage: String) : UiEvent {
  override val analyticsName = "Create Patient Entry:Colony or Village Text Changed"
}

data class PatientDistrictTextChanged(val district: String) : UiEvent {
  override val analyticsName = "Create Patient Entry:District Text Changed"
}

data class PatientStateTextChanged(val state: String) : UiEvent {
  override val analyticsName = "Create Patient Entry:State Text Changed"
}

class PatientEntrySaveClicked : UiEvent {
  override val analyticsName = "Create Patient Entry:Save Clicked"
}

data class OngoingPatientEntryChanged(val entry: OngoingPatientEntry) : UiEvent {
  override val analyticsName = "Create Patient Entry:Ongoing Patient Entry Changed"
}
