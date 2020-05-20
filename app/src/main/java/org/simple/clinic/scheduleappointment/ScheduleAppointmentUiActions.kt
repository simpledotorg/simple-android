package org.simple.clinic.scheduleappointment

import org.threeten.bp.LocalDate

interface ScheduleAppointmentUiActions {
  fun showManualDateSelector(date: LocalDate)
  fun closeSheet()
}
