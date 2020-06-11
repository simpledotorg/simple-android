package org.simple.clinic.registration.pin

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class RegistrationPinInit : Init<RegistrationPinModel, RegistrationPinEffect> {

  override fun init(model: RegistrationPinModel): First<RegistrationPinModel, RegistrationPinEffect> {
    return first(model)
  }
}
