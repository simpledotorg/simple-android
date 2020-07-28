package org.simple.clinic.forgotpin.createnewpin

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class ForgotPinCreateNewInit : Init<ForgotPinCreateNewModel, ForgotPinCreateNewEffect> {

  override fun init(model: ForgotPinCreateNewModel): First<ForgotPinCreateNewModel,
      ForgotPinCreateNewEffect> {

    val effects = mutableSetOf<ForgotPinCreateNewEffect>()

    if (model.hasUser.not()) {
      effects.add(LoadLoggedInUser)
    }

    if (model.hasFacility.not()) {
      effects.add(LoadCurrentFacility)
    }

    return first(model, effects)
  }
}
