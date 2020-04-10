package org.simple.clinic.patientcontact

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next
import org.simple.clinic.phone.PhoneNumberMaskerConfig

class PatientContactUpdate(
    private val proxyPhoneNumberForMaskedCalls: String
) : Update<PatientContactModel, PatientContactEvent, PatientContactEffect> {

  constructor(config: PhoneNumberMaskerConfig): this(proxyPhoneNumberForMaskedCalls = config.proxyPhoneNumber)

  override fun update(
      model: PatientContactModel,
      event: PatientContactEvent
  ): Next<PatientContactModel, PatientContactEffect> {
    return when(event) {
      is PatientProfileLoaded -> next(model.patientProfileLoaded(event.patientProfile))
      is OverdueAppointmentLoaded -> next(model.overdueAppointmentLoaded(event.overdueAppointment))
    }
  }
}
