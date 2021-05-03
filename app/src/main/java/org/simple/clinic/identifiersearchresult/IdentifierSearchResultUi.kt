package org.simple.clinic.identifiersearchresult

import org.simple.clinic.searchresultsview.PatientSearchResults

interface IdentifierSearchResultUi {
  fun showLoading()
  fun hideLoading()
  fun showSearchResults(foundPatients: PatientSearchResults)
  fun showSearchPatientButton()
  fun showNoPatientsMatched()
}
