package org.simple.clinic.search

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class SearchQueryTextChanged(val text: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Changed"
}

data class SearchQueryValidated(val validationErrors: List<PatientSearchValidationError>) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Validated"
}

class SearchClicked : UiEvent {
  override val analyticsName = "Patient Search:Search Clicked"
}

data class PatientItemClicked(val patientUuid: UUID): UiEvent {
  override val analyticsName = "Patient Search:Patient Item Clicked"
}
