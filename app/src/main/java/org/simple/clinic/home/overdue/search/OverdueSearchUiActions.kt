package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import org.simple.clinic.home.overdue.OverdueAppointment
import java.util.UUID

interface OverdueSearchUiActions {
  fun openPatientSummaryScreen(patientUuid: UUID)
  fun openContactPatientSheet(patientUuid: UUID)
  fun showOverdueSearchResults(searchResults: PagingData<OverdueAppointment>, searchQuery: String?)
  fun setOverdueSearchQuery(searchQuery: String)
}
