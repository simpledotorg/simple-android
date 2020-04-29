package org.simple.clinic.security.pin

import org.simple.clinic.security.pin.PinEntryUi.Mode

interface UiActions {

  fun hideError()
  fun showIncorrectPinErrorForFirstAttempt()
  fun showIncorrectPinErrorOnSubsequentAttempts(remaining: Int)
  fun showIncorrectAttemptsLimitReachedError(attemptsMade: Int)
  fun setPinEntryMode(mode: Mode)
  fun clearPin()
  fun pinVerified(data: Any?)
  fun showNetworkError()
  fun showServerError()
  fun showUnexpectedError()
}
