package org.simple.clinic.search.results

import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class PatientSearchResultsEvent : UiEvent

data class PatientSearchResultClicked(val patientUuid: UUID) : PatientSearchResultsEvent() {
  override val analyticsName = "Patient Search Results:Search Result Clicked"
}

object NewOngoingPatientEntrySaved : PatientSearchResultsEvent()

data class PatientSearchResultRegisterNewPatient(
    private val searchCriteria: PatientSearchCriteria
) : PatientSearchResultsEvent() {

  override val analyticsName: String
    get() {
      val criteriaAnalyticsName = when (searchCriteria) {
        is PatientSearchCriteria.Name -> "Name"
        is PatientSearchCriteria.PhoneNumber -> "Phone Number"
        is PatientSearchCriteria.NumericCriteria -> "Numeric Criteria"
      }
      return "Patient Search Results:Register New Patient:$criteriaAnalyticsName"
    }
}

