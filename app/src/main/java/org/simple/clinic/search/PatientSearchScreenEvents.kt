package org.simple.clinic.search

import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.UiEvent

class BackButtonClicked : UiEvent {
  override val analyticsName = "Patient Search:Back Clicked"
}

class CreateNewPatientClicked : UiEvent {
  override val analyticsName = "Patient Search:Create New Patient Clicked"
}

data class SearchQueryTextChanged(val query: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Changed"
}

data class SearchResultClicked(val searchResult: PatientSearchResult) : UiEvent {
  override val analyticsName = "Patient Search:Search Result Clicked"
}

data class SearchQueryAgeChanged(val ageString: String) : UiEvent {
  override val analyticsName = "Patient Search:Search Query Age Changed"
}
