package org.simple.clinic.home.overdue

interface OverdueUi {
  fun showOverdueAppointments(
      overdueAppointmentSections: OverdueAppointmentSections
  )

  fun showOverdueCount(count: Int)
  fun showProgress()
  fun hideProgress()
}
