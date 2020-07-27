package org.simple.clinic.forgotpin.createnewpin

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class ForgotPinCreateNewInit : Init<ForgotPinCreateNewModel, ForgotPinCreateNewEffect> {

  override fun init(model: ForgotPinCreateNewModel): First<ForgotPinCreateNewModel,
      ForgotPinCreateNewEffect> {
    return first(model)
  }
}
