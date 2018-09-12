package org.simple.clinic.scheduleappointment

import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.temporal.ChronoUnit

data class SheetCreated(val initialState: Int) : UiEvent

data class IncrementAppointmentDate(val currentState: Int, val size: Int) : UiEvent {
  override val analyticsName = "Schedule Appointment:Increment appointment due date"
}

data class DecrementAppointmentDate(val currentState: Int) : UiEvent {
  override val analyticsName = "Schedule Appointment:Decrement appointment due date"
}

data class ScheduleAppointment(val selectedDateState: Pair<String, Pair<Int, ChronoUnit>>) : UiEvent {
  override val analyticsName = "Schedule Appointment:Appointment scheduled"
}

class SkipScheduling : UiEvent {
  override val analyticsName = "Schedule Appointment:Scheduling skipped"
}
