package org.simple.clinic.registration.confirmpin

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.DoesNotMatchEnteredPin
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.NotValidated
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.Valid

class RegistrationConfirmPinUiRenderer(
    private val ui: RegistrationConfirmPinUi
) : ViewRenderer<RegistrationConfirmPinModel> {

  override fun render(model: RegistrationConfirmPinModel) {
    when (model.confirmPinValidationResult) {
      DoesNotMatchEnteredPin -> ui.showPinMismatchError()
      NotValidated, Valid -> {
        /* Nothing to do here from the UI perspective */
      }
    }
  }
}
