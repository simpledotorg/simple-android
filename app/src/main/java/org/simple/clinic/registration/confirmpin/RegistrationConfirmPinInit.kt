package org.simple.clinic.registration.confirmpin

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class RegistrationConfirmPinInit : Init<RegistrationConfirmPinModel, RegistrationConfirmPinEffect> {

  override fun init(model: RegistrationConfirmPinModel): First<RegistrationConfirmPinModel, RegistrationConfirmPinEffect> {
    return first(model)
  }
}
