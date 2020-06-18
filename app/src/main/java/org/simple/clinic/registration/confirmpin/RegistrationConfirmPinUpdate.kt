package org.simple.clinic.registration.confirmpin

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class RegistrationConfirmPinUpdate : Update<RegistrationConfirmPinModel, RegistrationConfirmPinEvent, RegistrationConfirmPinEffect> {

  override fun update(
      model: RegistrationConfirmPinModel,
      event: RegistrationConfirmPinEvent
  ): Next<RegistrationConfirmPinModel, RegistrationConfirmPinEffect> {
    return when (event) {
      is RegistrationConfirmPinTextChanged -> next(model.withEnteredPinConfirmation(event.confirmPin))
      is RegistrationConfirmPinDoneClicked -> dispatch(ValidatePinConfirmation(model.enteredPinConfirmation, model.ongoingRegistrationEntry))
      is PinConfirmationValidated -> validateEnteredPin(model, event)
    }
  }

  private fun validateEnteredPin(
      model: RegistrationConfirmPinModel,
      event: PinConfirmationValidated
  ): Next<RegistrationConfirmPinModel, RegistrationConfirmPinEffect> {
    val updatedModel = model.validatedPinConfirmation(event.result)

    return if (updatedModel.pinConfirmationDoesNotMatchEnteredPin) {
      next(updatedModel, ClearPin as RegistrationConfirmPinEffect)
    } else {
      next(updatedModel)
    }
  }
}
