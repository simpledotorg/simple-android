package org.simple.clinic.enterotp

interface EnterOtpUiActions {
  fun clearPin()
  fun goBack()
  fun showSmsSentMessage()
  fun showNetworkError()
  fun showUnexpectedError()
  fun showOtpEntryMode(mode: OtpEntryMode)
  fun hideError()
  fun showLimitReachedError(attemptsMade: Int)
  fun showIncorrectOtpErrorAttempt(attemptsMade: Int, attemptsRemaining: Int)
}
