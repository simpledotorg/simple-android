package org.simple.clinic.scheduleappointment

import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.widgets.UiEvent

sealed class ScheduleAppointmentEvent: UiEvent

data class DefaultAppointmentDateLoaded(val potentialAppointmentDate: PotentialAppointmentDate): ScheduleAppointmentEvent()

object AppointmentDateIncremented : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Increment appointment due date"
}
