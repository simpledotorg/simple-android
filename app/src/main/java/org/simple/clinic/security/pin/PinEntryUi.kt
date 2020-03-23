package org.simple.clinic.security.pin

interface PinEntryUi {

  // Not yet migrated to Mobius
  fun moveToState(state: PinEntryCardView.State)
  fun hideError()
  fun showIncorrectPinErrorForFirstAttempt()
  fun showIncorrectPinErrorOnSubsequentAttempts(remaining: Int)
  fun showIncorrectAttemptsLimitReachedError(attemptsMade: Int)
  fun clearPin()
  fun dispatchAuthenticatedCallback(enteredPin: String)
  fun setForgotButtonVisible(visible: Boolean)
}
