package org.simple.clinic.registerorlogin

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class AuthenticationInit : Init<AuthenticationModel, AuthenticationEffect> {

  override fun init(
      model: AuthenticationModel
  ): First<AuthenticationModel, AuthenticationEffect> {
    val effects = mutableSetOf<AuthenticationEffect>()

    when(model.openFor) {
      OpenFor.NewLogin -> effects.add(OpenCountrySelectionScreen)
      OpenFor.Reauthentication -> effects.add(OpenRegistrationPhoneScreen)
    }

    return first(model, effects)
  }
}
