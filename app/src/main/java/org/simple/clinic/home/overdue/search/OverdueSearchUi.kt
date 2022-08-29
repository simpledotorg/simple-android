package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import org.simple.clinic.home.overdue.OverdueAppointment
import java.util.UUID

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
  fun setOverdueSearchResultsPagingData(overdueSearchResults: PagingData<OverdueAppointment>, selectedAppointments: Set<UUID>)
}
