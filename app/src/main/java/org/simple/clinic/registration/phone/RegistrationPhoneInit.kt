package org.simple.clinic.registration.phone

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class RegistrationPhoneInit : Init<RegistrationPhoneModel, RegistrationPhoneEffect> {

  override fun init(model: RegistrationPhoneModel): First<RegistrationPhoneModel, RegistrationPhoneEffect> {
    return first(model)
  }
}
