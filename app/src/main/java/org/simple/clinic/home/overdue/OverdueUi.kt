package org.simple.clinic.home.overdue

interface OverdueUi: OverdueUiActions {
  fun updateList(overdueAppointments: List<OverdueAppointment>, isDiabetesManagementEnabled: Boolean)
  fun handleEmptyList(isEmpty: Boolean)
}
