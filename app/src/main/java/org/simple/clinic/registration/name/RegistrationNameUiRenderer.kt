package org.simple.clinic.registration.name

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.registration.name.RegistrationNameValidationResult.Blank
import org.simple.clinic.registration.name.RegistrationNameValidationResult.NotValidated
import org.simple.clinic.registration.name.RegistrationNameValidationResult.Valid
import org.simple.clinic.util.ValueChangedCallback

class RegistrationNameUiRenderer(
    private val ui: RegistrationNameUi
) : ViewRenderer<RegistrationNameModel> {

  private val validationChangedCallback = ValueChangedCallback<RegistrationNameValidationResult>()

  override fun render(model: RegistrationNameModel) {
    validationChangedCallback.pass(model.nameValidationResult) { validationResult ->
      when (validationResult) {
        NotValidated, Valid -> ui.hideValidationError()
        Blank -> ui.showEmptyNameValidationError()
      }
    }
  }
}
