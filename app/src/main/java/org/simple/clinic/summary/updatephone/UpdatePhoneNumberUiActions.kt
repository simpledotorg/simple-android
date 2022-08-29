package org.simple.clinic.summary.updatephone

interface UpdatePhoneNumberUiActions {
  fun preFillPhoneNumber(number: String)
  fun showBlankPhoneNumberError()
  fun showPhoneNumberTooShortError(minimumAllowedNumberLength: Int)
  fun closeDialog()
}
