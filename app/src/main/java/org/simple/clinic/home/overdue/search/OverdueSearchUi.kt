package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import org.simple.clinic.home.overdue.OverdueAppointment

interface OverdueSearchUi {
  fun showSearchHistory(searchHistory: Set<String>)
  fun hideSearchResults()
  fun hideSearchHistory()
  fun hideProgress()
  fun showProgress()
  fun showSearchResults()
  fun hideNoSearchResults()
  fun showNoSearchResults()
  fun setOverdueSearchResultsPagingData(overdueSearchResults: PagingData<OverdueAppointment>, searchQuery: String)
}
