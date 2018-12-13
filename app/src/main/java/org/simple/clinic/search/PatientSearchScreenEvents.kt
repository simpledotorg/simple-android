package org.simple.clinic.search

import org.simple.clinic.widgets.UiEvent

data class SearchQueryNameChanged(val name: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Changed"
}

data class SearchQueryValidated(val validationErrors: List<PatientSearchValidationError>) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Validated"
}

class SearchClicked : UiEvent {
  override val analyticsName = "Patient Search:Search Clicked"
}
