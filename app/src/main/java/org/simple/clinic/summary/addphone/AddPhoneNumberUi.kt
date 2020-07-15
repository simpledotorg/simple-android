package org.simple.clinic.summary.addphone

interface AddPhoneNumberUi : UiActions {
  fun showPhoneNumberBlank()
  fun showPhoneNumberTooShortError(requiredNumberLength: Int)
  fun showPhoneNumberTooLongError(requiredNumberLength: Int)
}
