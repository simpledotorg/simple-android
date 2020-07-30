package org.simple.clinic.forgotpin.confirmpin

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class ForgotPinConfirmPinUpdate : Update<ForgotPinConfirmPinModel, ForgotPinConfirmPinEvent,
    ForgotPinConfirmPinEffect> {
  override fun update(model: ForgotPinConfirmPinModel, event: ForgotPinConfirmPinEvent):
      Next<ForgotPinConfirmPinModel, ForgotPinConfirmPinEffect> {
    return when (event) {
      is LoggedInUserLoaded -> next(model.userLoaded(event.user))
    }
  }
}
