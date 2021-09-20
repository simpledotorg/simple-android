package org.simple.clinic.removeoverdueappointment

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.contactpatient.RemoveAppointmentReason
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.overdue.AppointmentCancelReason

class RemoveOverdueUpdate : Update<RemoveOverdueModel, RemoveOverdueEvent, RemoveOverdueEffect> {

  override fun update(
      model: RemoveOverdueModel,
      event: RemoveOverdueEvent
  ): Next<RemoveOverdueModel, RemoveOverdueEffect> {
    return when (event) {
      is RemoveAppointmentReasonSelected -> next(model.removeAppointmentReasonSelected(selectedReason = event.reason))
      is PatientMarkedAsMigrated -> dispatch(CancelAppointment(model.appointment.uuid, event.cancelReason))
      PatientMarkedAsDead -> dispatch(CancelAppointment(model.appointment.uuid, AppointmentCancelReason.Dead))
      PatientMarkedAsVisited,
      AppointmentMarkedAsCancelled -> dispatch(GoBackAfterAppointmentRemoval)
      DoneClicked -> removeAppointment(model)
    }
  }

  private fun removeAppointment(model: RemoveOverdueModel): Next<RemoveOverdueModel, RemoveOverdueEffect> {
    val appointmentUuid = model.appointment.uuid

    val effect = when (model.selectedReason!!) {
      RemoveAppointmentReason.AlreadyVisited -> MarkPatientAsVisited(appointmentUuid = appointmentUuid)
      RemoveAppointmentReason.Died -> MarkPatientAsDead(patientId = model.appointment.patientUuid, appointmentId = appointmentUuid)
      RemoveAppointmentReason.NotResponding -> CancelAppointment(appointmentUuid = appointmentUuid, reason = AppointmentCancelReason.PatientNotResponding)
      RemoveAppointmentReason.PhoneNumberNotWorking -> CancelAppointment(appointmentUuid = appointmentUuid, reason = AppointmentCancelReason.InvalidPhoneNumber)
      RemoveAppointmentReason.TransferredToAnotherFacility -> MarkPatientAsTransferredToAnotherFacility(patientId = model.appointment.patientUuid)
      RemoveAppointmentReason.MovedToPrivatePractitioner -> MarkPatientAsMovedToPrivate(patientId = model.appointment.patientUuid)
      RemoveAppointmentReason.OtherReason -> CancelAppointment(appointmentUuid = appointmentUuid, reason = AppointmentCancelReason.Other)
    }

    return dispatch(effect)
  }
}
