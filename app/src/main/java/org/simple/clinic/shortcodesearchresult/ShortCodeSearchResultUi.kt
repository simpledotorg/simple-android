package org.simple.clinic.shortcodesearchresult

import org.simple.clinic.searchresultsview.PatientSearchResults

interface ShortCodeSearchResultUi {
  fun showLoading()
  fun hideLoading()
  fun showSearchResults(foundPatients: PatientSearchResults)
  fun showSearchPatientButton()
  fun showNoPatientsMatched()
}
