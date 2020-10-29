package org.simple.clinic.registerorlogin

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first
import org.simple.clinic.registerorlogin.OpenFor.NewLogin
import org.simple.clinic.registerorlogin.OpenFor.Reauthentication

class AuthenticationInit : Init<AuthenticationModel, AuthenticationEffect> {

  override fun init(
      model: AuthenticationModel
  ): First<AuthenticationModel, AuthenticationEffect> {
    return if (!model.openedInitialScreen)
      loadInitialScreen(model)
    else
      first(model)
  }

  private fun loadInitialScreen(model: AuthenticationModel): First<AuthenticationModel, AuthenticationEffect> {
    val effect = when (model.openFor) {
      NewLogin -> OpenCountrySelectionScreen
      Reauthentication -> OpenRegistrationPhoneScreen
    }

    return first(model.initialScreenOpened(), effect)
  }
}
