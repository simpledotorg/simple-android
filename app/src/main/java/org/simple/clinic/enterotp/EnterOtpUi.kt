package org.simple.clinic.enterotp

interface EnterOtpUi {
  fun showUserPhoneNumber(phoneNumber: String)
  fun showIncorrectOtpError()
  fun hideError()
  fun showProgress()
  fun hideProgress()
  fun showOtpEntryMode(mode: OtpEntryMode)
  fun showFailedAttemptOtpError(attemptsRemaining: Int)
  fun showLimitReachedError(attemptsMade: Int)
  fun hideResendSmsButton()
  fun showResendSmsButton()
}
