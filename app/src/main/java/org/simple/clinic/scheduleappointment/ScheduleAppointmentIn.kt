package org.simple.clinic.scheduleappointment

import org.simple.clinic.scheduleappointment.TimeToAppointment.Days
import org.simple.clinic.scheduleappointment.TimeToAppointment.Months
import org.simple.clinic.scheduleappointment.TimeToAppointment.Weeks
import org.threeten.bp.temporal.ChronoUnit

// TODO: Use TimeToAppointment directly in the controller later
data class ScheduleAppointmentIn(val timeToAppointment: TimeToAppointment) {

  val timeAmount: Int
    get() = timeToAppointment.value

  val chronoUnit: ChronoUnit
    get() = when (timeToAppointment) {
      is Days -> ChronoUnit.DAYS
      is Weeks -> ChronoUnit.WEEKS
      is Months -> ChronoUnit.MONTHS
    }

  companion object {
    fun days(days: Int): ScheduleAppointmentIn {
      return ScheduleAppointmentIn(Days(days))
    }

    fun weeks(weeks: Int): ScheduleAppointmentIn {
      return ScheduleAppointmentIn(Weeks(weeks))
    }

    fun months(months: Int): ScheduleAppointmentIn {
      return ScheduleAppointmentIn(Months(months))
    }
  }
}
