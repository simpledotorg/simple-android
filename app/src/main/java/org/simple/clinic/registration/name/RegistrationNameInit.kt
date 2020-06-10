package org.simple.clinic.registration.name

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class RegistrationNameInit : Init<RegistrationNameModel, RegistrationNameEffect> {

  override fun init(model: RegistrationNameModel): First<RegistrationNameModel, RegistrationNameEffect> {
    return first(model, PrefillFields(model.ongoingRegistrationEntry))
  }
}
