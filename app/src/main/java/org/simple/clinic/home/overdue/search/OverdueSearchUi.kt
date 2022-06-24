package org.simple.clinic.home.overdue.search

interface OverdueSearchUi {
  fun showSearchHistory(searchHistory: Set<String>)
  fun hideSearchResults()
  fun hideSearchHistory()
  fun hideProgress()
  fun showProgress()
  fun showSearchResults()
  fun hideNoSearchResults()
  fun showNoSearchResults()
}
