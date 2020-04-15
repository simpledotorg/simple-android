package org.simple.clinic.contactpatient

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.threeten.bp.LocalDate

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
      is PatientMarkedAsAgreedToVisit -> dispatch(CloseScreen)
      is PatientAgreedToVisitClicked -> dispatch(MarkPatientAsAgreedToVisit(model.appointment!!.get().appointment.uuid))
      is NextReminderDateClicked -> selectNextReminderDate(model)
    }
  }

  private fun selectNextReminderDate(model: ContactPatientModel): Next<ContactPatientModel, ContactPatientEffect> {
    val reminderDate = findPotentialDateAfter(model.potentialAppointments, model.selectedAppointmentDate)

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
}
