package org.simple.clinic.removeoverdueappointment

import org.simple.clinic.contactpatient.RemoveAppointmentReason
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.widgets.UiEvent

sealed class RemoveOverdueEvent : UiEvent

data object PatientMarkedAsVisited : RemoveOverdueEvent()

data object PatientMarkedAsDead : RemoveOverdueEvent()

data object AppointmentMarkedAsCancelled : RemoveOverdueEvent()

data class PatientMarkedAsMigrated(val cancelReason: AppointmentCancelReason) : RemoveOverdueEvent()

data class RemoveAppointmentReasonSelected(val reason: RemoveAppointmentReason) : RemoveOverdueEvent() {

  override val analyticsName = "Contact Patient:Appointment cancel reason selected:$reason"
}

data object DoneClicked : RemoveOverdueEvent() {

  override val analyticsName = "Contact Patient:Done Clicked"
}
