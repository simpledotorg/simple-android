package org.simple.clinic.home.overdue

import androidx.paging.PagingData
import java.util.UUID

interface OverdueUiActions {
  fun openPhoneMaskBottomSheet(patientUuid: UUID)
  fun openPatientSummary(patientUuid: UUID)
  fun showOverdueAppointments(
      overdueAppointmentsOld: PagingData<OverdueAppointment_Old>,
      isDiabetesManagementEnabled: Boolean
  )

  fun showNoActiveNetworkConnectionDialog()
  fun openSelectDownloadFormatDialog()
  fun openSelectShareFormatDialog()
  fun openProgressForSharingDialog()
  fun openOverdueSearch()
}
