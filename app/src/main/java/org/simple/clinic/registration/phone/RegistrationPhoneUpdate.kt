package org.simple.clinic.registration.phone

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class RegistrationPhoneUpdate : Update<RegistrationPhoneModel, RegistrationPhoneEvent, RegistrationPhoneEffect> {

  override fun update(
      model: RegistrationPhoneModel,
      event: RegistrationPhoneEvent
  ): Next<RegistrationPhoneModel, RegistrationPhoneEffect> {
    return noChange()
  }
}
