package org.resolvetosavelives.red.newentry.personal

import org.resolvetosavelives.red.newentry.search.Gender
import org.resolvetosavelives.red.widgets.UiEvent

data class PatientFullNameTextChanged(val fullName: String) : UiEvent

data class PatientDateOfBirthTextChanged(val dateOfBirth: String) : UiEvent

data class PatientAgeTextChanged(val age: Int) : UiEvent

data class PatientGenderChanged(val gender: Gender) : UiEvent

class PatientPersonalDetailsProceedClicked : UiEvent
