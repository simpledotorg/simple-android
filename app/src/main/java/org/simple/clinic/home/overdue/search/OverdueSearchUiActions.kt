package org.simple.clinic.home.overdue.search

import java.util.UUID

interface OverdueSearchUiActions {
  fun openPatientSummaryScreen(patientUuid: UUID)
  fun openContactPatientSheet(patientUuid: UUID)
  fun setOverdueSearchQuery(searchQuery: String)
  fun openSelectDownloadFormatDialog(appointmentIds: Set<UUID>)
  fun showNoActiveNetworkConnectionDialog()
}
