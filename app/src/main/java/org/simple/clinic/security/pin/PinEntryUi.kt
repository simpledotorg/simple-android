package org.simple.clinic.security.pin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

interface PinEntryUi {

  // Not yet migrated to Mobius
  fun moveToState(state: State)
  fun hideError()
  fun showIncorrectPinErrorForFirstAttempt()
  fun showIncorrectPinErrorOnSubsequentAttempts(remaining: Int)
  fun showIncorrectAttemptsLimitReachedError(attemptsMade: Int)
  fun clearPin()
  fun dispatchAuthenticatedCallback(enteredPin: String)
  fun setForgotButtonVisible(visible: Boolean)
  
  sealed class State: Parcelable {

    @Parcelize
    object PinEntry : State()

    @Parcelize
    object Progress : State()

    @Parcelize
    data class BruteForceLocked(val timeTillUnlock: TimerDuration) : State()
  }
}
