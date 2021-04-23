package org.simple.clinic.searchresultsview

import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.widgets.UiEvent

sealed class SearchResultsEvent : UiEvent

data class SearchPatientWithCriteria(val criteria: PatientSearchCriteria) : SearchResultsEvent() {

  override val analyticsName: String = when (criteria) {
    is PatientSearchCriteria.Name -> "Search Results:Search By Patient Name"
    is PatientSearchCriteria.PhoneNumber -> "Search Results:Search By Patient Phone Number"
    is PatientSearchCriteria.NumericCriteria -> "Search Results:Search By Patient Numeric Identifier"
  }
}

data class SearchResultsLoaded(val patientSearchResults: PatientSearchResults) : SearchResultsEvent()

