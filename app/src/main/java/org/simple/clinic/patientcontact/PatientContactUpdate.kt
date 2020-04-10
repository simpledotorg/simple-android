package org.simple.clinic.patientcontact

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.phone.PhoneNumberMaskerConfig

class PatientContactUpdate(
    private val proxyPhoneNumberForMaskedCalls: String
) : Update<PatientContactModel, PatientContactEvent, PatientContactEffect> {

  constructor(config: PhoneNumberMaskerConfig) : this(proxyPhoneNumberForMaskedCalls = config.proxyPhoneNumber)

  override fun update(
      model: PatientContactModel,
      event: PatientContactEvent
  ): Next<PatientContactModel, PatientContactEffect> {
    return when (event) {
      is PatientProfileLoaded -> next(model.patientProfileLoaded(event.patientProfile))
      is OverdueAppointmentLoaded -> next(model.overdueAppointmentLoaded(event.overdueAppointment))
      is NormalCallClicked -> directlyCallPatient(model, event)
      is SecureCallClicked -> maskedCallPatient(model, event)
    }
  }

  private fun maskedCallPatient(
      model: PatientContactModel,
      event: SecureCallClicked
  ): Next<PatientContactModel, PatientContactEffect> {
    val patientPhoneNumber = model.patientProfile!!.phoneNumbers.first().number
    val effect = if (event.isPermissionGranted)
      MaskedCallWithAutomaticDialer(patientPhoneNumber = patientPhoneNumber, proxyPhoneNumber = proxyPhoneNumberForMaskedCalls)
    else
      MaskedCallWithManualDialer(patientPhoneNumber = patientPhoneNumber, proxyPhoneNumber = proxyPhoneNumberForMaskedCalls)

    return dispatch(effect)
  }

  private fun directlyCallPatient(
      model: PatientContactModel,
      event: NormalCallClicked
  ): Next<PatientContactModel, PatientContactEffect> {
    val patientPhoneNumber = model.patientProfile!!.phoneNumbers.first().number
    val effect = if (event.isPermissionGranted)
      DirectCallWithAutomaticDialer(patientPhoneNumber)
    else
      DirectCallWithManualDialer(patientPhoneNumber)

    return dispatch(effect)
  }
}
