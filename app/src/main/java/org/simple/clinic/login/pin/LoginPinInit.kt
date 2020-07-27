package org.simple.clinic.login.pin

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class LoginPinInit : Init<LoginPinModel, LoginPinEffect> {
  override fun init(model: LoginPinModel): First<LoginPinModel, LoginPinEffect> {
    return if (model.hasOngoingLoginEntry) {
      first(model)
    } else {
      first(model, LoadOngoingLoginEntry)
    }
  }
}
