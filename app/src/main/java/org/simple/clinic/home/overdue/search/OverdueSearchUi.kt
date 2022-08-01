package org.simple.clinic.home.overdue.search

interface OverdueSearchUi {
  fun hideSearchResults()
  fun hideProgress()
  fun showProgress()
  fun showSearchResults()
  fun hideNoSearchResults()
  fun showNoSearchResults()
  fun showDownloadAndShareButtons()
  fun hideDownloadAndShareButtons()
  fun showSelectedOverdueAppointmentCount(selectedOverdueAppointments: Int)
  fun hideSelectedOverdueAppointmentCount()
  fun setOverdueSearchSuggestions(searchSuggestions: List<String>)
}
