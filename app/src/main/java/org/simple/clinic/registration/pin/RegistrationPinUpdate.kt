package org.simple.clinic.registration.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class RegistrationPinUpdate: Update<RegistrationPinModel, RegistrationPinEvent, RegistrationPinEffect> {

  override fun update(model: RegistrationPinModel, event: RegistrationPinEvent): Next<RegistrationPinModel, RegistrationPinEffect> {
    return noChange()
  }
}
