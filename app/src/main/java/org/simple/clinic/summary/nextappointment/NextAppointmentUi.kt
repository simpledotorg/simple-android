package org.simple.clinic.summary.nextappointment

import java.time.LocalDate

interface NextAppointmentUi {
  fun showNoAppointment()
  fun showAppointmentDate(date: LocalDate)
  fun showAddAppointmentButton()
  fun showChangeAppointmentButton()
}
