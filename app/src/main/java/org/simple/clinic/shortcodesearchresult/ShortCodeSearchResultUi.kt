package org.simple.clinic.shortcodesearchresult

import org.simple.clinic.searchresultsview.PatientSearchResults
import java.util.UUID

interface ShortCodeSearchResultUi : UiActions {
  fun showLoading()
  fun hideLoading()
  fun showSearchResults(foundPatients: PatientSearchResults)
  fun showSearchPatientButton()
  fun showNoPatientsMatched()
}
