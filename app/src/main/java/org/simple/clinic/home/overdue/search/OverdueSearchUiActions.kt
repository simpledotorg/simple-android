package org.simple.clinic.home.overdue.search

import java.util.UUID

interface OverdueSearchUiActions {
  fun openPatientSummaryScreen(patientUuid: UUID)
  fun openContactPatientSheet(patientUuid: UUID)
  fun openSelectDownloadFormatDialog()
  fun openSelectShareFormatDialog()
  fun openShareInProgressDialog()
  fun showNoInternetConnectionDialog()
}
