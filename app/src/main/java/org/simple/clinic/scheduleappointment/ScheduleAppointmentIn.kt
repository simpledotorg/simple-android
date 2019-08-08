package org.simple.clinic.scheduleappointment

import org.threeten.bp.temporal.ChronoUnit

data class ScheduleAppointmentIn(
    val timeAmount: Int,
    val chronoUnit: ChronoUnit
) {
  companion object {
    fun days(days: Int): ScheduleAppointmentIn {
      return ScheduleAppointmentIn(days, ChronoUnit.DAYS)
    }

    fun weeks(weeks: Int): ScheduleAppointmentIn {
      return ScheduleAppointmentIn(weeks, ChronoUnit.WEEKS)
    }

    fun months(months: Int): ScheduleAppointmentIn {
      return ScheduleAppointmentIn(months, ChronoUnit.MONTHS)
    }
  }
}
