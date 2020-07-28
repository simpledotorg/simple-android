package org.simple.clinic.forgotpin.createnewpin

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class ForgotPinCreateNewInit : Init<ForgotPinCreateNewModel, ForgotPinCreateNewEffect> {

  override fun init(model: ForgotPinCreateNewModel): First<ForgotPinCreateNewModel,
      ForgotPinCreateNewEffect> {
    return if (model.hasUser.not()) {
      first(model, LoadLoggedInUser)
    } else {
      first(model)
    }
  }
}
