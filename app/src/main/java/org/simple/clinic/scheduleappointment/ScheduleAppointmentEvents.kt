package org.simple.clinic.scheduleappointment

import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet.ScheduleAppointment
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ScheduleAppointmentSheetCreated(val initialIndex: Int, val patientUuid: UUID) : UiEvent

data class AppointmentDateIncremented(val currentIndex: Int, val size: Int) : UiEvent {
  override val analyticsName = "Schedule Appointment:Increment appointment due date"
}

data class AppointmentDateDecremented(val currentIndex: Int, val size: Int) : UiEvent {
  override val analyticsName = "Schedule Appointment:Decrement appointment due date"
}

data class AppointmentScheduled(val selectedDateState: ScheduleAppointment) : UiEvent {
  override val analyticsName = "Schedule Appointment:Appointment scheduled"
}

class SchedulingSkipped : UiEvent {
  override val analyticsName = "Schedule Appointment:Scheduling skipped"
}
