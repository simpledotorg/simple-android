package org.simple.clinic.scheduleappointment

import java.time.LocalDate

interface ScheduleAppointmentUiActions {
  fun showManualDateSelector(date: LocalDate)
  fun closeSheet()
}
