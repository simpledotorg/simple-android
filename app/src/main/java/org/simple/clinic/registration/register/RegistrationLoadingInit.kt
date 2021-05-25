package org.simple.clinic.registration.register

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class RegistrationLoadingInit : Init<RegistrationLoadingModel, RegistrationLoadingEffect> {

  override fun init(model: RegistrationLoadingModel): First<RegistrationLoadingModel, RegistrationLoadingEffect> {
    return first(model, ConvertRegistrationEntryToUserDetails(model.registrationEntry))
  }
}
