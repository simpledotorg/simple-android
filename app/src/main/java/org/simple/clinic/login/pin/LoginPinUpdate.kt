package org.simple.clinic.login.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class LoginPinUpdate : Update<LoginPinModel, LoginPinEvent, LoginPinEffect> {
  override fun update(model: LoginPinModel, event: LoginPinEvent): Next<LoginPinModel, LoginPinEffect> {
    return noChange()
  }
}
