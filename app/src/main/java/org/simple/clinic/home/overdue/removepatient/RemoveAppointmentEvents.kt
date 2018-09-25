package org.simple.clinic.home.overdue.removepatient

import org.simple.clinic.overdue.Appointment
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class RemoveAppointmentSheetCreated(val appointmentUuid: UUID) : UiEvent

object RemoveReasonDoneClicked : UiEvent {
  override val analyticsName = "Remove Appointment with Reason:Done clicked"
}

object AlreadyVisitedReasonClicked : UiEvent {
  override val analyticsName = "Remove Appointment with Reason:Reason changed to patient already visited"
}

data class CancelReasonClicked(val selectedReason: Appointment.CancelReason) : UiEvent {
  override val analyticsName = "Remove Appointment with Reason:Reason changed to $selectedReason"
}
