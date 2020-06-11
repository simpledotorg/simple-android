package org.simple.clinic.registration.pin

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.registration.pin.RegistrationPinValidationResult.DoesNotMatchRequiredLength
import org.simple.clinic.registration.pin.RegistrationPinValidationResult.NotValidated
import org.simple.clinic.registration.pin.RegistrationPinValidationResult.Valid
import org.simple.clinic.util.ValueChangedCallback

class RegistrationPinUiRenderer(
    private val ui: RegistrationPinUi
) : ViewRenderer<RegistrationPinModel> {

  private val pinValidationChangedCallback = ValueChangedCallback<RegistrationPinValidationResult>()

  override fun render(model: RegistrationPinModel) {
    pinValidationChangedCallback.pass(model.pinValidationResult, ::toggleIncorrectPinLabel)
  }

  private fun toggleIncorrectPinLabel(pinValidationResult: RegistrationPinValidationResult) {
    when (pinValidationResult) {
      NotValidated, Valid -> ui.hideIncompletePinError()
      DoesNotMatchRequiredLength -> ui.showIncompletePinError()
    }
  }
}
