package org.simple.clinic.home.overdue

import java.util.UUID

interface OverdueUiActions {
  fun openPhoneMaskBottomSheet(patientUuid: UUID)
  fun showOverdueAppointments(dataSource: OverdueAppointmentRowDataSource.Factory)
  fun openPatientSummary(patientUuid: UUID)
}
