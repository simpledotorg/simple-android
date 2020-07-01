package org.simple.clinic.registration.register

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class RegistrationLoadingInit: Init<RegistrationLoadingModel, RegistrationLoadingEffect> {

  override fun init(model: RegistrationLoadingModel): First<RegistrationLoadingModel, RegistrationLoadingEffect> {
    return first(model)
  }
}
