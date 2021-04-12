package org.simple.clinic.contactpatient

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.contactpatient.RemoveAppointmentReason.AlreadyVisited
import org.simple.clinic.contactpatient.RemoveAppointmentReason.Died
import org.simple.clinic.contactpatient.RemoveAppointmentReason.MovedToPrivatePractitioner
import org.simple.clinic.contactpatient.RemoveAppointmentReason.NotResponding
import org.simple.clinic.contactpatient.RemoveAppointmentReason.OtherReason
import org.simple.clinic.contactpatient.RemoveAppointmentReason.PhoneNumberNotWorking
import org.simple.clinic.contactpatient.RemoveAppointmentReason.TransferredToAnotherFacility
import org.simple.clinic.contactpatient.UiMode.CallPatient
import org.simple.clinic.contactpatient.UiMode.RemoveAppointment
import org.simple.clinic.contactpatient.UiMode.SetAppointmentReminder
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentCancelReason.Other
import org.simple.clinic.overdue.AppointmentCancelReason.PatientNotResponding
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment.Days
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.util.daysTill
import java.time.LocalDate

class ContactPatientUpdate(
    private val proxyPhoneNumberForMaskedCalls: String
) : Update<ContactPatientModel, ContactPatientEvent, ContactPatientEffect> {

  constructor(config: PhoneNumberMaskerConfig) : this(proxyPhoneNumberForMaskedCalls = config.proxyPhoneNumber)

  override fun update(
      model: ContactPatientModel,
      event: ContactPatientEvent
  ): Next<ContactPatientModel, ContactPatientEffect> {
    return when (event) {
      is PatientProfileLoaded -> next(model.patientProfileLoaded(event.patientProfile))
      is OverdueAppointmentLoaded -> next(model.overdueAppointmentLoaded(event.overdueAppointment))
      is NormalCallClicked -> directlyCallPatient(model, event)
      is SecureCallClicked -> maskedCallPatient(model, event)
      PatientAgreedToVisitClicked -> dispatch(MarkPatientAsAgreedToVisit(model.appointmentUuid))
      NextReminderDateClicked -> selectNextReminderDate(model)
      PreviousReminderDateClicked -> selectPreviousReminderDate(model)
      is ManualDateSelected -> updateWithManuallySelectedDate(event, model)
      AppointmentDateClicked -> showManualDatePicker(model)
      SaveAppointmentReminderClicked -> {
        val appointmentUuid = model.appointmentUuid
        dispatch(SetReminderForAppointment(appointmentUuid, model.selectedAppointmentDate))
      }
      RemindToCallLaterClicked -> next(model.changeUiModeTo(SetAppointmentReminder))
      BackClicked -> backClicks(model)
      is RemoveAppointmentReasonSelected -> next(model.removeAppointmentReasonSelected(event.reason))
      RemoveAppointmentDoneClicked -> removeAppointment(model)
      RemoveFromOverdueListClicked -> dispatch(OpenRemoveOverdueAppointmentScreen(model.appointmentUuid, model.patientUuid))

      PatientMarkedAsAgreedToVisit,
      ReminderSetForAppointment,
      PatientMarkedAsVisited,
      PatientMarkedAsDead,
      AppointmentMarkedAsCancelled -> dispatch(CloseScreen)
      is PatientMarkAsMigrated -> dispatch(CancelAppointment(model.appointmentUuid, event.appointmentCancelReason))
    }
  }

  private fun removeAppointment(model: ContactPatientModel): Next<ContactPatientModel, ContactPatientEffect> {
    val appointmentUuid = model.appointmentUuid

    val effect = when (model.selectedRemoveAppointmentReason!!) {
      AlreadyVisited -> MarkPatientAsVisited(appointmentUuid = appointmentUuid)
      Died -> MarkPatientAsDead(patientUuid = model.patientUuid, appointmentUuid = appointmentUuid)
      NotResponding -> CancelAppointment(appointmentUuid = appointmentUuid, reason = PatientNotResponding)
      PhoneNumberNotWorking -> CancelAppointment(appointmentUuid = appointmentUuid, reason = InvalidPhoneNumber)
      TransferredToAnotherFacility -> MarkPatientAsTransferredToAnotherFacility(patientUuid = model.patientUuid)
      MovedToPrivatePractitioner -> MarkPatientAsMovedToPrivate(patientUuid = model.patientUuid)
      OtherReason -> CancelAppointment(appointmentUuid = appointmentUuid, reason = Other)
    }

    return dispatch(effect)
  }

  private fun backClicks(model: ContactPatientModel): Next<ContactPatientModel, ContactPatientEffect> {
    return when (model.uiMode) {
      CallPatient -> dispatch(CloseScreen as ContactPatientEffect)
      SetAppointmentReminder -> next(model.changeUiModeTo(CallPatient))
      RemoveAppointment -> next(model.changeUiModeTo(CallPatient))
    }
  }

  private fun showManualDatePicker(model: ContactPatientModel): Next<ContactPatientModel, ContactPatientEffect> {
    val earliestPossibleAppointmentDate = model.potentialAppointments.first().scheduledFor
    val latestPossibleAppointmentDate = model.potentialAppointments.last().scheduledFor

    val datePickerBounds = earliestPossibleAppointmentDate..latestPossibleAppointmentDate

    return dispatch(ShowManualDatePicker(model.selectedAppointmentDate, datePickerBounds))
  }

  private fun updateWithManuallySelectedDate(
      event: ManualDateSelected,
      model: ContactPatientModel
  ): Next<ContactPatientModel, ContactPatientEffect> {
    val selectedDate = event.selectedDate
    val currentDate = event.currentDate

    val matchingPotentialAppointmentDate = model.potentialAppointments.firstOrNull { it.scheduledFor == selectedDate }

    val reminderDate = matchingPotentialAppointmentDate ?: PotentialAppointmentDate(selectedDate, Days(currentDate daysTill selectedDate))

    return next(model.reminderDateSelected(reminderDate))
  }

  private fun selectNextReminderDate(model: ContactPatientModel): Next<ContactPatientModel, ContactPatientEffect> {
    val reminderDate = findPotentialDateAfter(model.potentialAppointments, model.selectedAppointmentDate)

    return if (reminderDate != null) {
      next(model.reminderDateSelected(reminderDate))
    } else {
      noChange()
    }
  }

  private fun selectPreviousReminderDate(model: ContactPatientModel): Next<ContactPatientModel, ContactPatientEffect> {
    val reminderDate = findPotentialAppointmentBefore(model.potentialAppointments, model.selectedAppointmentDate)

    return if (reminderDate != null) {
      next(model.reminderDateSelected(reminderDate))
    } else {
      noChange()
    }
  }

  private fun maskedCallPatient(
      model: ContactPatientModel,
      event: SecureCallClicked
  ): Next<ContactPatientModel, ContactPatientEffect> {
    val patientPhoneNumber = model.patientProfile!!.phoneNumbers.first().number
    val effect = if (event.isPermissionGranted)
      MaskedCallWithAutomaticDialer(patientPhoneNumber = patientPhoneNumber, proxyPhoneNumber = proxyPhoneNumberForMaskedCalls)
    else
      MaskedCallWithManualDialer(patientPhoneNumber = patientPhoneNumber, proxyPhoneNumber = proxyPhoneNumberForMaskedCalls)

    return dispatch(effect)
  }

  private fun directlyCallPatient(
      model: ContactPatientModel,
      event: NormalCallClicked
  ): Next<ContactPatientModel, ContactPatientEffect> {
    val patientPhoneNumber = model.patientProfile!!.phoneNumbers.first().number
    val effect = if (event.isPermissionGranted)
      DirectCallWithAutomaticDialer(patientPhoneNumber)
    else
      DirectCallWithManualDialer(patientPhoneNumber)

    return dispatch(effect)
  }

  private fun findPotentialDateAfter(
      potentialAppointmentDates: List<PotentialAppointmentDate>,
      date: LocalDate
  ): PotentialAppointmentDate? {
    return potentialAppointmentDates.firstOrNull { it.scheduledFor > date }
  }

  private fun findPotentialAppointmentBefore(
      potentialAppointmentDates: List<PotentialAppointmentDate>,
      date: LocalDate
  ): PotentialAppointmentDate? {
    return potentialAppointmentDates.lastOrNull { it.scheduledFor < date }
  }
}
