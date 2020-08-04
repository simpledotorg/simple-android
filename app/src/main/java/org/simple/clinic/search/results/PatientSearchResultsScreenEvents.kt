package org.simple.clinic.search.results

import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.widgets.UiEvent

class PatientSearchResultsScreenCreated : UiEvent

data class PatientSearchResultRegisterNewPatient(private val searchCriteria: PatientSearchCriteria) : UiEvent {

  override val analyticsName: String
    get() {
      val criteriaAnalyticsName = when (searchCriteria) {
        is PatientSearchCriteria.Name -> "Name"
        is PatientSearchCriteria.PhoneNumber -> "Phone Number"
      }
      return "Patient Search Results:Register New Patient:$criteriaAnalyticsName"
    }
}
