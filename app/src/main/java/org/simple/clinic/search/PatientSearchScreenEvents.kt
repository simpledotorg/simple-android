package org.simple.clinic.search

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class SearchQueryTextChanged(val text: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Changed"
}

sealed class SearchQueryValidationResult : UiEvent {

  data class Valid(val text: String) : SearchQueryValidationResult() {
    override val analyticsName = "Patient Search:Search Query Validated:Valid"
  }

  data class Invalid(val errors: List<PatientSearchValidationError>) : SearchQueryValidationResult() {
    override val analyticsName = "Patient Search:Search Query Validated:Invalid"
  }
}

class SearchClicked : UiEvent {
  override val analyticsName = "Patient Search:Search Clicked"
}

data class PatientItemClicked(val patientUuid: UUID) : UiEvent {
  override val analyticsName = "Patient Search:Patient Item Clicked"
}
