package org.simple.clinic.registration.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class RegistrationPinUpdate: Update<RegistrationPinModel, RegistrationPinEvent, RegistrationPinEffect> {

  override fun update(model: RegistrationPinModel, event: RegistrationPinEvent): Next<RegistrationPinModel, RegistrationPinEffect> {
    return when(event) {
      is CurrentOngoingEntrySaved -> dispatch(ProceedToConfirmPin)
    }
  }
}
