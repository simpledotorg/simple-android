package org.simple.clinic.registration.name

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class RegistrationNameInit: Init<RegistrationNameModel, RegistrationNameEffect> {

  override fun init(model: RegistrationNameModel): First<RegistrationNameModel, RegistrationNameEffect> {
    return first(model)
  }
}
