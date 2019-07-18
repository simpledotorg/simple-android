package org.simple.clinic.searchresultsview

import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.searchresultsview.SearchPatientBy.Name
import org.simple.clinic.searchresultsview.SearchPatientBy.PhoneNumber
import org.simple.clinic.widgets.UiEvent

object SearchResultsViewCreated : UiEvent {
  override val analyticsName = "Search Results:View Created"
}

data class SearchResultClicked(val searchResult: PatientSearchResult) : UiEvent {
  override val analyticsName = "Search Results:Search Result Clicked"
}

data class SearchResultPatientName(val patientName: String) : UiEvent {
  override val analyticsName = "Search Results:Patient Name"
}

object RegisterNewPatientClicked : UiEvent {
  override val analyticsName = "Search Results:Register New Patient Clicked"
}

data class RegisterNewPatient(val patientName: String) : UiEvent {
  override val analyticsName = "Search Results:Register New Patient"
}

data class SearchPatientCriteria(val searchPatientBy: SearchPatientBy) : UiEvent {

  override val analyticsName: String = when (searchPatientBy) {
    is Name -> "Search Results:Search By Patient Name"
    is PhoneNumber -> "Search Results:Search By Patient Phone Number"
  }
}
