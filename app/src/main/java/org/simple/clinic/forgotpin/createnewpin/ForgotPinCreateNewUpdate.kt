package org.simple.clinic.forgotpin.createnewpin

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class ForgotPinCreateNewUpdate : Update<ForgotPinCreateNewModel, ForgotPinCreateNewEvent,
    ForgotPinCreateNewEffect> {

  override fun update(model: ForgotPinCreateNewModel, event: ForgotPinCreateNewEvent):
      Next<ForgotPinCreateNewModel, ForgotPinCreateNewEffect> {
    return noChange()
  }
}
