package org.simple.clinic.addidtopatient.searchresults

import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.UiEvent

data class AddIdToPatientSearchResultsScreenCreated(
    val criteria: PatientSearchCriteria,
    val identifier: Identifier
) : UiEvent {

  val patientName: String
    get() = when (criteria) {
      is PatientSearchCriteria.Name -> criteria.patientName
      is PatientSearchCriteria.PhoneNumber -> ""
    }
}

data class AddIdToPatientSearchResultClicked(val searchResult: PatientSearchResult) : UiEvent {
  override val analyticsName = "Add ID to Patient Search Results:Search Result Clicked"
}

object AddIdToPatientSearchResultRegisterNewPatientClicked : UiEvent {
  override val analyticsName = "Add ID to Patient Search Results:Register New Patient"
}
