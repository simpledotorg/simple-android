package org.simple.clinic.scheduleappointment

import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import java.util.UUID

data class ScheduleAppointmentSheetCreated(
    val patientUuid: UUID
) : UiEvent

object AppointmentDateIncremented : UiEvent {
  override val analyticsName = "Schedule Appointment:Increment appointment due date"
}

object AppointmentDateDecremented : UiEvent {
  override val analyticsName = "Schedule Appointment:Decrement appointment due date"
}

object AppointmentDone : UiEvent {
  override val analyticsName = "Schedule Appointment:Appointment done"
}

object ManuallySelectAppointmentDateClicked : UiEvent {
  override val analyticsName = "Schedule Appointment:Manually Select Appointment Date"
}

data class AppointmentCalendarDateSelected(val selectedDate: LocalDate) : UiEvent {
  override val analyticsName = "Schedule Appointment:Appointment calendar date selected"
}

object SchedulingSkipped : UiEvent {
  override val analyticsName = "Schedule Appointment:Scheduling skipped"
}
