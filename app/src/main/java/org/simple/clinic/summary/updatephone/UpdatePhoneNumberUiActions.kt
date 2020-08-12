package org.simple.clinic.summary.updatephone

interface UpdatePhoneNumberUiActions {
  fun preFillPhoneNumber(number: String)
  fun showBlankPhoneNumberError()
  fun showPhoneNumberTooShortError(minimumAllowedNumberLength: Int)
  fun showPhoneNumberTooLongError(maximumRequiredNumberLength: Int)
  fun closeDialog()
}
