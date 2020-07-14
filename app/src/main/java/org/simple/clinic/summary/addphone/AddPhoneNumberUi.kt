package org.simple.clinic.summary.addphone

interface AddPhoneNumberUi {
  fun showPhoneNumberTooShortError()
  fun showPhoneNumberTooLongError()
  fun closeDialog()
}
