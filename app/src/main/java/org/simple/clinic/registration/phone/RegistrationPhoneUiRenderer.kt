package org.simple.clinic.registration.phone

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class RegistrationPhoneUiRenderer(
    private val ui: RegistrationPhoneUi
) : ViewRenderer<RegistrationPhoneModel> {

  private val phoneValidationResultValueCallback = ValueChangedCallback<RegistrationPhoneValidationResult>()

  override fun render(model: RegistrationPhoneModel) {
    if (model.isInPhoneEntryMode) {
      renderPhoneNumberValidationResult(model)
    }
  }

  private fun renderPhoneNumberValidationResult(model: RegistrationPhoneModel) {
    if (model.phoneValidationResult != null) {
      phoneValidationResultValueCallback.pass(model.phoneValidationResult, ::togglePhoneValidationLabel)
    } else {
      ui.hideAnyError()
    }
  }

  private fun togglePhoneValidationLabel(result: RegistrationPhoneValidationResult) {
    if (result !is RegistrationPhoneValidationResult.Valid) {
      ui.showInvalidNumberError()
    } else {
      ui.hideAnyError()
    }
  }
}
