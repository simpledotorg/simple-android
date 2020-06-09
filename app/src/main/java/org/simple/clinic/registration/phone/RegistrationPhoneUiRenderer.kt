package org.simple.clinic.registration.phone

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.registration.phone.RegistrationResult.Completed
import org.simple.clinic.registration.phone.RegistrationResult.NetworkError
import org.simple.clinic.registration.phone.RegistrationResult.OtherError
import org.simple.clinic.util.ValueChangedCallback

class RegistrationPhoneUiRenderer(
    private val ui: RegistrationPhoneUi
) : ViewRenderer<RegistrationPhoneModel> {

  private val phoneValidationResultValueCallback = ValueChangedCallback<RegistrationPhoneValidationResult>()

  private val registrationResultValueCallback = ValueChangedCallback<RegistrationResult>()

  private val uiModeChangeCallback = ValueChangedCallback<RegistrationUiMode>()

  override fun render(model: RegistrationPhoneModel) {
    uiModeChangeCallback.pass(model.mode) { mode ->
      if (mode == RegistrationUiMode.RegistrationOngoing) {
        registrationResultValueCallback.clear()
        ui.showProgressIndicator()
      } else {
        ui.hideProgressIndicator()
      }
    }

    renderPhoneNumberValidationResult(model)
    renderRegistrationResult(model)
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

  private fun renderRegistrationResult(model: RegistrationPhoneModel) {
    if (model.registrationResult != null) {
      registrationResultValueCallback.pass(model.registrationResult, ::setErrorMessageForRegistrationResult)
    }
  }

  private fun setErrorMessageForRegistrationResult(result: RegistrationResult) {
    when (result) {
      Completed -> ui.hideAnyError()
      NetworkError -> ui.showNetworkErrorMessage()
      OtherError -> ui.showUnexpectedErrorMessage()
    }
  }
}
