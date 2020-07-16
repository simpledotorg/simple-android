package org.simple.clinic.home.overdue

interface OverdueUi {
  fun updateList(overdueAppointments: List<OverdueAppointment>, isDiabetesManagementEnabled: Boolean)
  fun handleEmptyList(isEmpty: Boolean)
}
