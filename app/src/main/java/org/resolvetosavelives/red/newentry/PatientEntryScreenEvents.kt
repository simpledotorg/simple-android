package org.resolvetosavelives.red.newentry

import org.resolvetosavelives.red.patient.Gender
import org.resolvetosavelives.red.util.Optional
import org.resolvetosavelives.red.widgets.UiEvent

data class PatientFullNameTextChanged(val fullName: String) : UiEvent

data class PatientPhoneNumberTextChanged(val phoneNumber: String) : UiEvent

data class PatientNoPhoneNumberToggled(val noneSelected: Boolean) : UiEvent

data class PatientDateOfBirthTextChanged(val dateOfBirth: String) : UiEvent

data class PatientAgeTextChanged(val age: String) : UiEvent

data class PatientGenderChanged(val gender: Optional<Gender>) : UiEvent

data class PatientColonyOrVillageTextChanged(val colonyOrVillage: String) : UiEvent

data class PatientDistrictTextChanged(val district: String) : UiEvent

data class PatientStateTextChanged(val state: String) : UiEvent

class PatientEntrySaveClicked : UiEvent
