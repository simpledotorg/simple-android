package org.simple.clinic.shortcodesearchresult

import org.simple.clinic.patient.PatientSearchResult
import java.util.UUID

interface ShortCodeSearchResultUi {
  fun openPatientSummary(patientUuid: UUID)
  fun openPatientSearch()
  fun showLoading()
  fun hideLoading()
  fun showSearchResults(foundPatients: List<PatientSearchResult>)
  fun showSearchPatientButton()
  fun showNoPatientsMatched()
}
