package org.simple.clinic.security.pin

import org.simple.clinic.security.pin.PinEntryUi.State

interface UiActions {

  fun hideError()
  fun showIncorrectPinErrorForFirstAttempt()
  fun showIncorrectPinErrorOnSubsequentAttempts(remaining: Int)
  fun showIncorrectAttemptsLimitReachedError(attemptsMade: Int)
  fun moveToState(state: State)
  fun clearPin()
  fun dispatchAuthenticatedCallback(enteredPin: String)
}
