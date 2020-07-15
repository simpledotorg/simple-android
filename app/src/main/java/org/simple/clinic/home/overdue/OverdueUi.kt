package org.simple.clinic.home.overdue

import java.util.UUID

interface OverdueUi {
  fun updateList(overdueAppointments: List<OverdueAppointment>, isDiabetesManagementEnabled: Boolean)
  fun handleEmptyList(isEmpty: Boolean)
  fun openPhoneMaskBottomSheet(patientUuid: UUID)
}
