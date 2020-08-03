package org.simple.clinic.forgotpin.confirmpin

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class ForgotPinConfirmPinUpdate : Update<ForgotPinConfirmPinModel, ForgotPinConfirmPinEvent,
    ForgotPinConfirmPinEffect> {
  override fun update(model: ForgotPinConfirmPinModel, event: ForgotPinConfirmPinEvent):
      Next<ForgotPinConfirmPinModel, ForgotPinConfirmPinEffect> = noChange()
}
