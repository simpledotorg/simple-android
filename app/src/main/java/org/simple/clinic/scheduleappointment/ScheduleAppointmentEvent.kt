package org.simple.clinic.scheduleappointment

import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate

sealed class ScheduleAppointmentEvent : UiEvent

data class DefaultAppointmentDateLoaded(val potentialAppointmentDate: PotentialAppointmentDate) : ScheduleAppointmentEvent()

object AppointmentDateIncremented : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Increment appointment due date"
}

object AppointmentDateDecremented : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Decrement appointment due date"
}

data class AppointmentCalendarDateSelected(val selectedDate: LocalDate) : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Appointment calendar date selected"
}

object ManuallySelectAppointmentDateClicked : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Manually Select Appointment Date"
}
