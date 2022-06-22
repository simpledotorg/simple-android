package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import org.simple.clinic.home.overdue.OverdueAppointment

interface OverdueSearchUi {
  fun showSearchHistory(searchHistory: Set<String>)
  fun hideSearchResults()
  fun showOverdueSearchResults(overdueSearchResults: PagingData<OverdueAppointment>)
  fun hideSearchHistory()
}
