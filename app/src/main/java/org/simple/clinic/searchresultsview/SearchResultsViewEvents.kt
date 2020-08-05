package org.simple.clinic.searchresultsview

import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

object SearchResultsViewCreated : UiEvent {
  override val analyticsName = "Search Results:View Created"
}

data class SearchResultClicked(val patientUuid: UUID) : UiEvent {
  override val analyticsName = "Search Results:Search Result Clicked"
}

data class SearchPatientWithCriteria(val criteria: PatientSearchCriteria) : UiEvent {

  override val analyticsName: String = when (criteria) {
    is Name -> "Search Results:Search By Patient Name"
    is PhoneNumber -> "Search Results:Search By Patient Phone Number"
  }
}
