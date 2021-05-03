package org.simple.clinic.shortcodesearchresult

import org.simple.clinic.searchresultsview.PatientSearchResults

interface IdentifierSearchResultUi {
  fun showLoading()
  fun hideLoading()
  fun showSearchResults(foundPatients: PatientSearchResults)
  fun showSearchPatientButton()
  fun showNoPatientsMatched()
}
