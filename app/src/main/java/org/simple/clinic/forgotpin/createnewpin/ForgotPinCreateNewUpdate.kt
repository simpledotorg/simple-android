package org.simple.clinic.forgotpin.createnewpin

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class ForgotPinCreateNewUpdate : Update<ForgotPinCreateNewModel, ForgotPinCreateNewEvent,
    ForgotPinCreateNewEffect> {

  override fun update(model: ForgotPinCreateNewModel, event: ForgotPinCreateNewEvent):
      Next<ForgotPinCreateNewModel, ForgotPinCreateNewEffect> {
    return when (event) {
      is LoggedInUserLoaded -> next(model.userLoaded(event.user))
      is CurrentFacilityLoaded -> next(model.facilityLoaded(event.facility))
      is PinValidated -> pinValidated(model, event)
      is ForgotPinCreateNewPinTextChanged -> next(model.pinChanged(event.pin), HideInvalidPinError)
      ForgotPinCreateNewPinSubmitClicked -> dispatch(ValidatePin(model.pin))
    }
  }

  private fun pinValidated(
      model: ForgotPinCreateNewModel,
      event: PinValidated
  ): Next<ForgotPinCreateNewModel, ForgotPinCreateNewEffect> {
    return if (event.isValid)
      dispatch(ShowConfirmPinScreen(model.pin!!))
    else
      dispatch(ShowInvalidPinError)
  }
}
