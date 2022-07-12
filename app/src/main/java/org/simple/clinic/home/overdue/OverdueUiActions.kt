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
  fun openSelectDownloadFormatDialog(selectedAppointmentIds: Set<UUID>)
  fun openSelectShareFormatDialog(selectedAppointmentIds: Set<UUID>)
  fun openProgressForSharingDialog(selectedAppointmentIds: Set<UUID>)
  fun openOverdueSearch()
}
