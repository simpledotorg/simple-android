package org.simple.clinic.summary.addphone

interface AddPhoneNumberUi : UiActions {
  fun showPhoneNumberTooShortError()
  fun showPhoneNumberTooLongError()
  fun closeDialog()
}
