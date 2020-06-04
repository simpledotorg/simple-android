package org.simple.clinic.registration.phone

import org.simple.clinic.mobius.ViewRenderer

class RegistrationPhoneUiRenderer(
    private val ui: RegistrationPhoneUi
) : ViewRenderer<RegistrationPhoneModel> {

  override fun render(model: RegistrationPhoneModel) {
    if (model.isInPhoneEntryMode) {
      renderPhoneNumberValidationResult(model)
    }
  }

  private fun renderPhoneNumberValidationResult(model: RegistrationPhoneModel) {
    if (model.phoneValidationResult != null && model.phoneValidationResult !is RegistrationPhoneValidationResult.Valid) {
      ui.showInvalidNumberError()
    }
  }
}
