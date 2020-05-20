package org.simple.clinic.scheduleappointment

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

object AppointmentDone : UiEvent {
  override val analyticsName = "Schedule Appointment:Appointment done"
}

object SchedulingSkipped : UiEvent {
  override val analyticsName = "Schedule Appointment:Scheduling skipped"
}

data class PatientFacilityChanged(val facilityUuid: UUID) : UiEvent {
  override val analyticsName = "Schedule Appointment: Patient facility changed"
}
