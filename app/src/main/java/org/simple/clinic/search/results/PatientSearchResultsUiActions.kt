package org.simple.clinic.search.results

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface PatientSearchResultsUiActions {
  fun openPatientSummaryScreen(patientUuid: UUID)
  fun openLinkIdWithPatientScreen(patientUuid: UUID, identifier: Identifier)
  fun openPatientEntryScreen(facility: Facility)
}
