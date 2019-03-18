package org.simple.clinic.home.overdue.removepatient

import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class RemoveAppointmentSheetCreated(val appointmentUuid: UUID) : UiEvent

object RemoveReasonDoneClicked : UiEvent {
  override val analyticsName = "Remove Appointment with Reason:Done clicked"
}

data class PatientDeadClicked(val patientUuid: UUID) : UiEvent {
  override val analyticsName = "Remove Appointment with Reason:Reason changed to Patient Dead"
}

data class CancelReasonClicked(val selectedReason: AppointmentCancelReason) : UiEvent {
  override val analyticsName = "Remove Appointment with Reason:Reason changed to $selectedReason"
}

object PatientAlreadyVisitedClicked : UiEvent {
  override val analyticsName = "Remove Appointment with Reason:Reason changed to Patient Already Visited"
}
