package org.simple.clinic.home.overdue

import androidx.paging.PagingData
import java.util.*

interface OverdueUiActions {
  fun openPhoneMaskBottomSheet(patientUuid: UUID)
  fun showOverdueAppointments(dataSource: OverdueAppointmentRowDataSource.Factory)
  fun openPatientSummary(patientUuid: UUID)
  fun showOverdueAppointments(
      overdueAppointments: PagingData<OverdueAppointment>,
      isDiabetesManagementEnabled: Boolean
  )
}
