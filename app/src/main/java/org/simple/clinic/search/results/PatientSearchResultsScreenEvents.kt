package org.simple.clinic.search.results

import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.UiEvent

data class PatientSearchResultsScreenCreated(val key: PatientSearchResultsScreenKey) : UiEvent

data class PatientSearchResultClicked(val searchResult: PatientSearchResult) : UiEvent {
  override val analyticsName = "Patient Search:Search Result Clicked"
}

class CreateNewPatientClicked : UiEvent {
  override val analyticsName = "Patient Search:Create New Patient Clicked"
}
