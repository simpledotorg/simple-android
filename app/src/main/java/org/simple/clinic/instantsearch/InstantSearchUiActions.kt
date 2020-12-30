package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import java.util.UUID

interface InstantSearchUiActions {
  fun showPatientsSearchResults(patients: List<PatientSearchResult>, facility: Facility)
  fun openPatientSummary(patientId: UUID)
}
