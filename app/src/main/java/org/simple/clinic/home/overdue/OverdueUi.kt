package org.simple.clinic.home.overdue

import java.util.UUID

interface OverdueUi {
  fun showOverdueAppointments(
      overdueAppointmentSections: OverdueAppointmentSections,
      selectedOverdueAppointments: Set<UUID>,
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
