package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface InstantSearchUiActions {
  fun showAllPatients(patients: List<PatientSearchResult>, facility: Facility)
  fun showPatientsSearchResults(patients: List<PatientSearchResult>, facility: Facility, searchQuery: String)
  fun openPatientSummary(patientId: UUID)
  fun openLinkIdWithPatientScreen(patientId: UUID, identifier: Identifier)
  fun openBpPassportSheet(identifier: Identifier)
  fun showNoPatientsInFacility(facility: Facility)
  fun showNoSearchResults()
  fun hideNoPatientsInFacility()
  fun hideNoSearchResults()
  fun openPatientEntryScreen(facility: Facility)
  fun showKeyboard()
  fun openShortCodeSearchScreen(shortCode: String)
  fun openQrCodeScanner()
}
