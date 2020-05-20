package org.simple.clinic.scheduleappointment

import org.simple.clinic.widgets.UiEvent

object AppointmentDone : UiEvent {
  override val analyticsName = "Schedule Appointment:Appointment done"
}

object SchedulingSkipped : UiEvent {
  override val analyticsName = "Schedule Appointment:Scheduling skipped"
}
