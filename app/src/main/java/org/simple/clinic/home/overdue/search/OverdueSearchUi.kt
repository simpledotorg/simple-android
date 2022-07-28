package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import org.simple.clinic.home.overdue.OverdueAppointment
import java.util.UUID

interface OverdueSearchUi {
  fun showSearchHistory(searchHistory: Set<String>)
  fun hideSearchResults()
  fun hideSearchHistory()
  fun hideProgress()
  fun showProgress()
  fun showSearchResults()
  fun hideNoSearchResults()
  fun showNoSearchResults()
  fun setOverdueSearchResultsPagingData(
      overdueSearchResults: PagingData<OverdueAppointment>,
      selectedOverdueAppointments: Set<UUID>,
      searchQuery: String
  )

  fun showDownloadAndShareButtons()
  fun hideDownloadAndShareButtons()
  fun showSelectedOverdueAppointmentCount(selectedOverdueAppointments: Int)
  fun hideSelectedOverdueAppointmentCount()
  fun setOverdueSearchSuggestions(searchSuggestions: List<String>)
}
