package org.simple.clinic.search.results

import java.util.UUID

interface PatientSearchResultsUiActions {
  fun openPatientSummaryScreen(patientUuid: UUID)
}
