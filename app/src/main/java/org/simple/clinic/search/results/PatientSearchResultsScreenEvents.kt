package org.simple.clinic.search.results

import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.UiEvent

data class PatientSearchResultsScreenCreated(val key: PatientSearchResultsScreenKey) : UiEvent

data class PatientSearchResultClicked(val searchResult: PatientSearchResult) : UiEvent {
  override val analyticsName = "Patient Search Results:Search Result Clicked"
}

data class PatientSearchResultRegisterNewPatient(val patientName: String) : UiEvent {
  override val analyticsName = "Patient Search Results:Register New Patient"
}
