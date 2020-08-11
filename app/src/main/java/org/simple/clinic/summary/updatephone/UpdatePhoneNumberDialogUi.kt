package org.simple.clinic.summary.updatephone

interface UpdatePhoneNumberDialogUi : UpdatePhoneNumberUiActions {
  fun showPhoneNumberTooShortError()
  fun showPhoneNumberTooLongError()
  fun closeDialog()
}
