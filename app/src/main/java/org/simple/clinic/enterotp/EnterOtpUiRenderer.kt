package org.simple.clinic.enterotp

import org.simple.clinic.enterotp.ValidationResult.IsNotRequiredLength
import org.simple.clinic.enterotp.ValidationResult.NotValidated
import org.simple.clinic.enterotp.ValidationResult.Valid
import org.simple.clinic.mobius.ViewRenderer

class EnterOtpUiRenderer(
    private val ui: EnterOtpUi
) : ViewRenderer<EnterOtpModel> {

  override fun render(model: EnterOtpModel) {
    if (model.hasLoadedUser) {
      ui.showUserPhoneNumber(model.user!!.phoneNumber)
    }

    when(model.otpValidationResult) {
      NotValidated -> { /* Nothing to do here */ }
      IsNotRequiredLength -> { ui.showIncorrectOtpError() }
      Valid -> { /* Nothing to do here */ }
    }
  }
}
