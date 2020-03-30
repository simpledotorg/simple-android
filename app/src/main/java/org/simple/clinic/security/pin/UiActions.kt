package org.simple.clinic.security.pin

import org.simple.clinic.security.pin.PinEntryUi.Mode

interface UiActions {

  fun hideError()
  fun showIncorrectPinErrorForFirstAttempt()
  fun showIncorrectPinErrorOnSubsequentAttempts(remaining: Int)
  fun showIncorrectAttemptsLimitReachedError(attemptsMade: Int)
  fun setPinEntryMode(mode: Mode)
  fun clearPin()
  fun dispatchAuthenticatedCallback(enteredPin: String)
  fun pinVerified(data: Any?)
}
