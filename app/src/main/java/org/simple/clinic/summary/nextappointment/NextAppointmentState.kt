package org.simple.clinic.summary.nextappointment

import java.time.LocalDate

sealed interface NextAppointmentState {
  data object NoAppointment : NextAppointmentState

  data class Today(val date: LocalDate) : NextAppointmentState

  data class Scheduled(val date: LocalDate, val daysRemaining: Int) : NextAppointmentState

  data class Overdue(val date: LocalDate, val overdueDays: Int) : NextAppointmentState
}
