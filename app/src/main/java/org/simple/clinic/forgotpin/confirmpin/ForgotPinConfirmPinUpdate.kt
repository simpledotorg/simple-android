package org.simple.clinic.forgotpin.confirmpin

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.user.resetpin.ResetPinResult

class ForgotPinConfirmPinUpdate : Update<ForgotPinConfirmPinModel, ForgotPinConfirmPinEvent,
    ForgotPinConfirmPinEffect> {
  override fun update(model: ForgotPinConfirmPinModel, event: ForgotPinConfirmPinEvent):
      Next<ForgotPinConfirmPinModel, ForgotPinConfirmPinEffect> {
    return when (event) {
      is LoggedInUserLoaded -> next(model.userLoaded(event.user))
      is CurrentFacilityLoaded -> next(model.facilityLoaded(event.facility))
      is ForgotPinConfirmPinTextChanged -> dispatch(HideError)
      is ForgotPinConfirmPinSubmitClicked -> dispatch(ValidatePinConfirmation(model.previousPin, event.pin))
      is PinConfirmationValidated -> pinConfirmationValidated(event)
      is PatientSyncAndResetPinCompleted -> patientSyncAndResetPinCompleted(event)
    }
  }

  private fun patientSyncAndResetPinCompleted(event: PatientSyncAndResetPinCompleted): Next<ForgotPinConfirmPinModel, ForgotPinConfirmPinEffect> {
    val effect = when (event.resetPinResult) {
      ResetPinResult.Success -> GoToHomeScreen
      ResetPinResult.NetworkError -> ShowNetworkError
      ResetPinResult.UserNotFound,
      is ResetPinResult.UnexpectedError -> ShowUnexpectedError
    }

    return dispatch(effect)
  }

  private fun pinConfirmationValidated(
      event: PinConfirmationValidated
  ): Next<ForgotPinConfirmPinModel, ForgotPinConfirmPinEffect> {
    return if (event.isValid.not()) {
      dispatch(ShowMismatchedError)
    } else {
      dispatch(ShowProgress, SyncPatientDataAndResetPin(event.newPin))
    }
  }
}
