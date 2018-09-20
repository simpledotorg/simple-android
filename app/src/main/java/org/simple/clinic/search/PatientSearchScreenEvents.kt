package org.simple.clinic.search

import org.simple.clinic.widgets.UiEvent

data class SearchQueryNameChanged(val name: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Changed"
}

data class SearchQueryDateOfBirthChanged(val dateOfBirth: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Date Of Birth Changed"
}

data class SearchQueryAgeChanged(val ageString: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Age Changed"
}

data class SearchQueryValidated(val validationErrors: List<PatientSearchValidationError>): UiEvent {
  override val analyticsName = "Patient Search:Search Query Validated"
}

class SearchClicked : UiEvent {
  override val analyticsName = "Patient Search:Search Clicked"
}
