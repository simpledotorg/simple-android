package org.simple.clinic.summary.updatephone

interface UpdatePhoneNumberDialogUi : UpdatePhoneNumberUiActions {
  fun showPhoneNumberTooLongError()
  fun closeDialog()
}
