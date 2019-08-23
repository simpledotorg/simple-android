package org.simple.clinic.shortcodesearchresult

import org.simple.clinic.allpatientsinfacility.PatientSearchResultUiState
import java.util.UUID

interface ShortCodeSearchResultUi {
  fun openPatientSummary(patientUuid: UUID)
  fun openPatientSearch()
  fun showLoading()
  fun hideLoading()
  fun showSearchResults(foundPatients: List<PatientSearchResultUiState>)
  fun showSearchPatientButton()
  fun showNoPatientsMatched()
}
