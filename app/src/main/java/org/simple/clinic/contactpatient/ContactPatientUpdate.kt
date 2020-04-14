package org.simple.clinic.contactpatient

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.phone.PhoneNumberMaskerConfig

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
}
