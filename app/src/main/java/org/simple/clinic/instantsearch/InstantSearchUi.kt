package org.simple.clinic.instantsearch

interface InstantSearchUi {
  fun showProgress()
  fun hideProgress()

  fun showNoSearchResults()
  fun hideNoSearchResults()

  fun showNoPatientsInFacility(facilityName: String)
  fun hideNoPatientsInFacility()

  fun showResults()
  fun hideResults()
}
