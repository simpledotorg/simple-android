package org.simple.clinic.home.overdue

interface OverdueUi {
  fun showOverdueAppointments(
      overdueAppointmentSections: OverdueAppointmentSections,
      overdueListSectionStates: OverdueListSectionStates
  )

  fun showOverdueCount(count: Int)
  fun showProgress()
  fun hideProgress()

  fun showNoOverduePatientsView()
  fun hideNoOverduePatientsView()

  fun showOverdueRecyclerView()
  fun hideOverdueRecyclerView()
}
