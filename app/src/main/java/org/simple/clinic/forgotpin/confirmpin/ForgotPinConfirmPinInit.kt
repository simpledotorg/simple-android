package org.simple.clinic.forgotpin.confirmpin

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class ForgotPinConfirmPinInit : Init<ForgotPinConfirmPinModel, ForgotPinConfirmPinEffect> {
  override fun init(model: ForgotPinConfirmPinModel): First<ForgotPinConfirmPinModel, ForgotPinConfirmPinEffect> {
    val effects = mutableSetOf<ForgotPinConfirmPinEffect>()
    if (model.hasUser.not()) {
      effects.add(LoadLoggedInUser)
    }

    if (model.hasFacility.not()) {
      effects.add(LoadCurrentFacility)
    }

    return first(model, effects)
  }
}
