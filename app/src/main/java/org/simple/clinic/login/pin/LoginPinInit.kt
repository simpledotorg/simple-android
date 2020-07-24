package org.simple.clinic.login.pin

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class LoginPinInit : Init<LoginPinModel, LoginPinEffect> {
  override fun init(model: LoginPinModel): First<LoginPinModel, LoginPinEffect> {
    return first(model)
  }
}
