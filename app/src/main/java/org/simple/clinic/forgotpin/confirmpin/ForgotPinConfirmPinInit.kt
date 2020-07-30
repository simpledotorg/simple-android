package org.simple.clinic.forgotpin.confirmpin

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class ForgotPinConfirmPinInit : Init<ForgotPinConfirmPinModel, ForgotPinConfirmPinEffect> {
  override fun init(model: ForgotPinConfirmPinModel): First<ForgotPinConfirmPinModel,
      ForgotPinConfirmPinEffect> {
    return first(model, LoadLoggedInUser)
  }
}
