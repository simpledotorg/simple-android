package org.simple.clinic.search.results

import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.UiEvent

data class PatientSearchResultsScreenCreated(val key: PatientSearchResultsScreenKey) : UiEvent

data class PatientSearchResultClicked(val searchResult: PatientSearchResult) : UiEvent {
  override val analyticsName = "Patient Search Results:Search Result Clicked"
}

data class PatientSearchResultRegisterNewPatient(val searchCriteria: PatientSearchCriteria) : UiEvent {

  constructor(patientName: String) : this(PatientSearchCriteria.Name(patientName))

  val patientName: String
    get() = (searchCriteria as PatientSearchCriteria.Name).patientName

  override val analyticsName: String
    get() {
      val criteriaAnalyticsName = when (searchCriteria) {
        is PatientSearchCriteria.Name -> "Name"
        is PatientSearchCriteria.PhoneNumber -> "Phone Number"
      }
      return "Patient Search Results:Register New Patient:$criteriaAnalyticsName"
    }
}
