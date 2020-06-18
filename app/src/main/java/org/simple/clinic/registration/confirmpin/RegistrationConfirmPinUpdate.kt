package org.simple.clinic.registration.confirmpin

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class RegistrationConfirmPinUpdate : Update<RegistrationConfirmPinModel, RegistrationConfirmPinEvent, RegistrationConfirmPinEffect> {

  override fun update(
      model: RegistrationConfirmPinModel,
      event: RegistrationConfirmPinEvent
  ): Next<RegistrationConfirmPinModel, RegistrationConfirmPinEffect> {
    return noChange()
  }
}
