package org.simple.clinic.home.overdue

import java.util.UUID

interface OverdueUi {
  fun showOverdueAppointments(
      overdueAppointmentSections: OverdueAppointmentSections,
      selectedOverdueAppointments: Set<UUID>,
      overdueListSectionStates: OverdueListSectionStates
  )

  fun showOverdueCount(count: Int)

  fun showSelectedOverdueAppointmentCount(selectedOverdueAppointments: Int)
  fun hideSelectedOverdueAppointmentCount()

  fun showProgress()
  fun hideProgress()

  fun showNoOverduePatientsView()
  fun hideNoOverduePatientsView()

  fun showOverdueAppointmentSections()
  fun hideOverdueAppointmentSections()
}
