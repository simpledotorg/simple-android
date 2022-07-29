package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import org.simple.clinic.home.overdue.OverdueAppointment
import java.util.UUID

interface OverdueSearchUiActions {
  fun openPatientSummaryScreen(patientUuid: UUID)
  fun openContactPatientSheet(patientUuid: UUID)
  fun setOverdueSearchQuery(searchQuery: String)
  fun openSelectDownloadFormatDialog()
  fun openSelectShareFormatDialog()
  fun openShareInProgressDialog()
  fun showNoInternetConnectionDialog()
  fun setOverdueSearchResultsPagingData(
      overdueSearchResults: PagingData<OverdueAppointment>,
      selectedOverdueAppointments: Set<UUID>,
      searchQuery: String
  )
}
