package org.simple.clinic.addidtopatient.searchresults

import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class AddIdToPatientSearchResultsScreenCreated(
    val searchCriteria: PatientSearchCriteria,
    val identifier: Identifier
) : UiEvent

data class AddIdToPatientSearchResultClicked(val patientUuid: UUID) : UiEvent {
  override val analyticsName = "Add ID to Patient Search Results:Search Result Clicked"
}

object AddIdToPatientSearchResultRegisterNewPatientClicked : UiEvent {
  override val analyticsName = "Add ID to Patient Search Results:Register New Patient"
}
