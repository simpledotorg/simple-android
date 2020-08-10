package org.simple.clinic.enterotp

interface EnterOtpUi {
  fun showUserPhoneNumber(phoneNumber: String)
  fun showUnexpectedError()
  fun showNetworkError()
  fun showServerError(error: String)
  fun showIncorrectOtpError()
  fun hideError()
  fun showProgress()
  fun hideProgress()
}
