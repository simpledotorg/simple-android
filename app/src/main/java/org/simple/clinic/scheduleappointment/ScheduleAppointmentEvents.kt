package org.simple.clinic.scheduleappointment

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ScheduleAppointmentSheetCreated(val defaultDateIndex: Int, val patientUuid: UUID, val numberOfDates: Int) : UiEvent

data class ScheduleAppointmentSheetCreated2(
    val possibleAppointments: List<ScheduleAppointment>,
    val defaultAppointment: ScheduleAppointment
) : UiEvent

data class AppointmentDateIncremented(val currentIndex: Int, val size: Int) : UiEvent {
  override val analyticsName = "Schedule Appointment:Increment appointment due date"
}

object AppointmentDateIncremented2 : UiEvent {
  override val analyticsName = "Schedule Appointment:Increment appointment due date 2"
}

data class AppointmentDateDecremented(val currentIndex: Int, val size: Int) : UiEvent {
  override val analyticsName = "Schedule Appointment:Decrement appointment due date"
}

object AppointmentDateDecremented2 : UiEvent {
  override val analyticsName = "Schedule Appointment:Decrement appointment due date 2"
}

object AppointmentChooseCalendarClicks : UiEvent {
  override val analyticsName = "Schedule Appointment:Choose calendar clicks"
}

data class AppointmentScheduled(val selectedDateState: ScheduleAppointment) : UiEvent {
  override val analyticsName = "Schedule Appointment:Appointment scheduled"
}

data class AppointmentCalendarDateSelected(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int
) : UiEvent {
  override val analyticsName = "Schedule Appointment:Appointment calendar date selected"
}

class SchedulingSkipped : UiEvent {
  override val analyticsName = "Schedule Appointment:Scheduling skipped"
}
