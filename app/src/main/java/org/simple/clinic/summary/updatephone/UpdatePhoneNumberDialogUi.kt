package org.simple.clinic.summary.updatephone

interface UpdatePhoneNumberDialogUi {
  fun showPhoneNumberTooShortError()
  fun showPhoneNumberTooLongError()
  fun preFillPhoneNumber(number: String)
  fun closeDialog()
}
