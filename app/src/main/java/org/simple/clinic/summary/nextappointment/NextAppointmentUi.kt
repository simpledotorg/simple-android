package org.simple.clinic.summary.nextappointment

import java.time.LocalDate

interface NextAppointmentUi {
  fun showNoAppointment()
  fun showAddAppointmentButton()
  fun showChangeAppointmentButton()
  fun showAppointmentDate(date: LocalDate)
  fun showAppointmentFacility(name: String)
  fun hideAppointmentFacility()
}
