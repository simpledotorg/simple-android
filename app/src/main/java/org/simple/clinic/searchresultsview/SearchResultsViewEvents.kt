package org.simple.clinic.searchresultsview

import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.searchresultsview.SearchPatientInput.Name
import org.simple.clinic.searchresultsview.SearchPatientInput.PhoneNumber
import org.simple.clinic.widgets.UiEvent

object SearchResultsViewCreated : UiEvent {
  override val analyticsName = "Search Results:View Created"
}

data class SearchResultClicked(val searchResult: PatientSearchResult) : UiEvent {
  override val analyticsName = "Search Results:Search Result Clicked"
}

object RegisterNewPatientClicked : UiEvent {
  override val analyticsName = "Search Results:Register New Patient Clicked"
}

data class RegisterNewPatient(val searchPatientInput: SearchPatientInput) : UiEvent {
  override val analyticsName = "Search Results:Register New Patient"
}

data class SearchPatientWithInput(val searchPatientInput: SearchPatientInput) : UiEvent {

  override val analyticsName: String = when (searchPatientInput) {
    is Name -> "Search Results:Search By Patient Name"
    is PhoneNumber -> "Search Results:Search By Patient Phone Number"
  }
}
