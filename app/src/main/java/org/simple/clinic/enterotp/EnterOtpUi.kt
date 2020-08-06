package org.simple.clinic.enterotp

interface EnterOtpUi: EnterOtpUiActions {
  fun showUserPhoneNumber(phoneNumber: String)
  fun goBack()
  fun showUnexpectedError()
  fun showNetworkError()
  fun showServerError(error: String)
  fun showIncorrectOtpError()
  fun hideError()
  fun showProgress()
  fun hideProgress()
  fun showSmsSentMessage()
  fun clearPin()
}
