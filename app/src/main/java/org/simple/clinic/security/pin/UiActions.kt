package org.simple.clinic.security.pin

interface UiActions {

  fun hideError()
  fun showIncorrectPinErrorForFirstAttempt()
  fun showIncorrectPinErrorOnSubsequentAttempts(remaining: Int)
  fun showIncorrectAttemptsLimitReachedError(attemptsMade: Int)
}
