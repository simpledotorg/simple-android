package org.simple.clinic.enterotp

interface EnterOtpUiActions {
  fun clearPin()
  fun goBack()
  fun showSmsSentMessage()
  fun showNetworkError()
  fun showUnexpectedError()
  fun showOtpEntryMode(mode: OtpEntryMode)
}
