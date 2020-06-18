package org.simple.clinic.registration.confirmpin

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class RegistrationConfirmPinUpdate : Update<RegistrationConfirmPinModel, RegistrationConfirmPinEvent, RegistrationConfirmPinEffect> {

  override fun update(
      model: RegistrationConfirmPinModel,
      event: RegistrationConfirmPinEvent
  ): Next<RegistrationConfirmPinModel, RegistrationConfirmPinEffect> {
    return when(event) {
      is RegistrationConfirmPinTextChanged -> next(model.withEnteredPinConfirmation(event.confirmPin))
      is PinConfirmationValidated -> next(model.validatedPinConfirmation(event.result))
    }
  }
}
