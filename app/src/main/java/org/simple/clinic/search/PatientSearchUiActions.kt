package org.simple.clinic.search

import org.simple.clinic.patient.PatientSearchCriteria
import java.util.UUID

interface PatientSearchUiActions {
  fun openSearchResultsScreen(criteria: PatientSearchCriteria)
  fun openPatientSummary(patientUuid: UUID)
}
