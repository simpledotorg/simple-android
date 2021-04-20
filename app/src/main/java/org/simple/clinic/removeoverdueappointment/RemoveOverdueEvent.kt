package org.simple.clinic.removeoverdueappointment

import org.simple.clinic.contactpatient.RemoveAppointmentReason
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.widgets.UiEvent

sealed class RemoveOverdueEvent : UiEvent

object PatientMarkedAsVisited : RemoveOverdueEvent()

object PatientMarkedAsDead : RemoveOverdueEvent()

object AppointmentMarkedAsCancelled : RemoveOverdueEvent()

data class PatientMarkedAsMigrated(val cancelReason: AppointmentCancelReason) : RemoveOverdueEvent()

data class RemoveAppointmentReasonSelected(val reason: RemoveAppointmentReason) : RemoveOverdueEvent() {

  override val analyticsName = "Contact Patient:Appointment cancel reason selected:$reason"
}

object DoneClicked : RemoveOverdueEvent() {

  override val analyticsName = "Contact Patient:Done Clicked"
}
