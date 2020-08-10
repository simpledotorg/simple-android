package org.simple.clinic.main

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.main.LifecycleEvent.ActivityDestroyed
import org.simple.clinic.main.LifecycleEvent.ActivityStarted
import org.simple.clinic.main.LifecycleEvent.ActivityStopped
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus

private val SHOW_APP_LOCK_FOR_USER_STATES = setOf(
    LoggedInStatus.OTP_REQUESTED,
    LoggedInStatus.LOGGED_IN,
    LoggedInStatus.RESET_PIN_REQUESTED
)

class TheActivityUpdate : Update<TheActivityModel, TheActivityEvent, TheActivityEffect> {

  override fun update(model: TheActivityModel, event: TheActivityEvent): Next<TheActivityModel, TheActivityEffect> {
    return when (event) {
      ActivityStarted -> dispatch(LoadAppLockInfo)
      ActivityStopped -> noChange()
      ActivityDestroyed -> noChange()
      is AppLockInfoLoaded -> {
        val userOptional = event.user

        if (userOptional.isPresent()) {
          val hasAppLockTimerExpired = event.currentTimestamp.isAfter(event.lockAtTimestamp)
          val shouldShowAppLockScreen = shouldShowAppLockScreenForUser(userOptional.get()) && hasAppLockTimerExpired

          val effect = if (shouldShowAppLockScreen) ShowAppLockScreen else ClearLockAfterTimestamp

          dispatch(effect)
        } else {
          noChange()
        }
      }
    }
  }

  private fun shouldShowAppLockScreenForUser(
      user: User
  ): Boolean {
    return user.isNotDisapprovedForSyncing && user.loggedInStatus in SHOW_APP_LOCK_FOR_USER_STATES
  }
}
