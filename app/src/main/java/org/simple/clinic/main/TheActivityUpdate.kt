package org.simple.clinic.main

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.main.LifecycleEvent.ActivityDestroyed
import org.simple.clinic.main.LifecycleEvent.ActivityStarted
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus
import java.time.Instant

private val SHOW_APP_LOCK_FOR_USER_STATES = setOf(
    LoggedInStatus.OTP_REQUESTED,
    LoggedInStatus.LOGGED_IN,
    LoggedInStatus.RESET_PIN_REQUESTED
)

class TheActivityUpdate : Update<TheActivityModel, TheActivityEvent, TheActivityEffect> {

  override fun update(model: TheActivityModel, event: TheActivityEvent): Next<TheActivityModel, TheActivityEffect> {
    return when (event) {
      ActivityStarted -> dispatch(LoadAppLockInfo)
      ActivityDestroyed -> noChange()
      is AppLockInfoLoaded -> handleScreenLock(event)
      UserWasJustVerified -> dispatch(ShowUserLoggedOutOnOtherDeviceAlert)
      UserWasUnauthorized -> dispatch(RedirectToLoginScreen)
      UserWasDisapproved -> dispatch(ClearPatientData)
      PatientDataCleared -> dispatch(ShowAccessDeniedScreen)
    }
  }

  private fun handleScreenLock(event: AppLockInfoLoaded): Next<TheActivityModel, TheActivityEffect> {
    val userOptional = event.user

    return if (userOptional.isPresent()) {
      val effect = screenLockEffect(
          currentTimestamp = event.currentTimestamp,
          lockAtTimestamp = event.lockAtTimestamp,
          user = userOptional.get()
      )

      dispatch(effect)
    } else {
      noChange()
    }
  }

  private fun screenLockEffect(
      currentTimestamp: Instant,
      lockAtTimestamp: Instant,
      user: User
  ): TheActivityEffect {
    val hasAppLockTimerExpired = currentTimestamp.isAfter(lockAtTimestamp)
    val shouldShowAppLockScreen = shouldShowAppLockScreenForUser(user) && hasAppLockTimerExpired

    return if (shouldShowAppLockScreen) ShowAppLockScreen else ClearLockAfterTimestamp
  }

  private fun shouldShowAppLockScreenForUser(
      user: User
  ): Boolean {
    return user.isNotDisapprovedForSyncing && user.loggedInStatus in SHOW_APP_LOCK_FOR_USER_STATES
  }
}
