package org.simple.clinic.registration.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class RegistrationPinUpdate(
    private val requiredPinLength: Int
) : Update<RegistrationPinModel, RegistrationPinEvent, RegistrationPinEffect> {

  override fun update(
      model: RegistrationPinModel,
      event: RegistrationPinEvent
  ): Next<RegistrationPinModel, RegistrationPinEffect> {
    return when (event) {
      is RegistrationPinTextChanged -> next(model.pinChanged(event.pin))
      is RegistrationPinDoneClicked -> doneClicked(model)
    }
  }

  private fun doneClicked(model: RegistrationPinModel): Next<RegistrationPinModel, RegistrationPinEffect> {
    val updatedModel = validateModel(model)

    return if (updatedModel.isEnteredPinValid)
      next(updatedModel, ProceedToConfirmPin(model.ongoingRegistrationEntry))
    else
      next(updatedModel)
  }

  private fun validateModel(model: RegistrationPinModel): RegistrationPinModel {
    return if (model.isEnteredPinOfLength(requiredPinLength))
      model.validPinEntered()
    else
      model.pinDoesNotMatchRequiredLength()
  }
}
