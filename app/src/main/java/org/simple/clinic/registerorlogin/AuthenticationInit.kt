package org.simple.clinic.registerorlogin

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class AuthenticationInit : Init<AuthenticationModel, AuthenticationEffect> {

  override fun init(
      model: AuthenticationModel
  ): First<AuthenticationModel, AuthenticationEffect> {
    return first(model)
  }
}
