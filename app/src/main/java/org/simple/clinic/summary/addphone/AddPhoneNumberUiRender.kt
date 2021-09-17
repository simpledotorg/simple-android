package org.simple.clinic.summary.addphone

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.Blank
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooShort
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.ValidNumber

class AddPhoneNumberUiRender(private val ui: AddPhoneNumberUi) : ViewRenderer<AddPhoneNumberModel> {
  override fun render(model: AddPhoneNumberModel) {
    if (model.hasValidationResult) {
      renderValidationErrors(model.validationResult!!)
    }
  }

  private fun renderValidationErrors(validationResult: Result) {
    when (validationResult) {
      is LengthTooShort -> ui.showPhoneNumberTooShortError(validationResult.minimumAllowedNumberLength)
      Blank -> ui.showPhoneNumberBlank()
      ValidNumber -> ui.clearPhoneNumberError()
    }
  }
}
