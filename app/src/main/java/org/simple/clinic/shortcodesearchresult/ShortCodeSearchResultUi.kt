package org.simple.clinic.shortcodesearchresult

import org.simple.clinic.searchresultsview.PatientSearchResults
import java.util.UUID

interface ShortCodeSearchResultUi {
  fun openPatientSummary(patientUuid: UUID)
  fun openPatientSearch()
  fun showLoading()
  fun hideLoading()
  fun showSearchResults(foundPatients: PatientSearchResults)
  fun showSearchPatientButton()
  fun showNoPatientsMatched()
}
