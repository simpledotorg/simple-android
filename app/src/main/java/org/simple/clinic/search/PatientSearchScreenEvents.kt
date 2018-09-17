package org.simple.clinic.search

import org.simple.clinic.patient.PatientSearchResult
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

class CreateNewPatientClicked : UiEvent {
  override val analyticsName = "Patient Search:Create New Patient Clicked"
}

data class SearchResultClicked(val searchResult: PatientSearchResult) : UiEvent {
  override val analyticsName = "Patient Search:Search Result Clicked"
}

class SearchClicked : UiEvent {
  override val analyticsName = "Patient Search:Search Clicked"
}
