package org.simple.clinic.summary.nextappointment

import java.time.LocalDate

interface NextAppointmentUi {
  fun showNoAppointment()
  fun showAddAppointmentButton()
  fun showChangeAppointmentButton()
  fun showAppointmentDate(date: LocalDate)
  fun showAppointmentDateWithRemainingDays(date: LocalDate, daysRemaining: Int)
  fun showAppointmentDateWithOverdueDays(date: LocalDate, overdueDays: Int)
  fun showAppointmentFacility(name: String)
  fun hideAppointmentFacility()
}
