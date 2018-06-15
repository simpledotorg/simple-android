package org.simple.clinic.newentry

import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent

data class PatientFullNameTextChanged(val fullName: String) : UiEvent

data class PatientPhoneNumberTextChanged(val phoneNumber: String) : UiEvent

data class PatientNoPhoneNumberToggled(val noneSelected: Boolean) : UiEvent

data class PatientNoColonyOrVillageToggled(val noneSelected: Boolean) : UiEvent

data class PatientDateOfBirthTextChanged(val dateOfBirth: String) : UiEvent

data class PatientDateOfBirthFocusChanged(val hasFocus: Boolean) : UiEvent

data class PatientAgeTextChanged(val age: String) : UiEvent

data class PatientGenderChanged(val gender: Optional<Gender>) : UiEvent

data class PatientColonyOrVillageTextChanged(val colonyOrVillage: String) : UiEvent

data class PatientDistrictTextChanged(val district: String) : UiEvent

data class PatientStateTextChanged(val state: String) : UiEvent

class PatientEntrySaveClicked : UiEvent

data class OngoingPatientEntryChanged(val entry: OngoingPatientEntry) : UiEvent
