package org.simple.clinic.main

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Optional
import java.time.Instant

private val SHOW_APP_LOCK_FOR_USER_STATES = setOf(
    LoggedInStatus.OTP_REQUESTED,
    LoggedInStatus.LOGGED_IN,
    LoggedInStatus.RESET_PIN_REQUESTED
)

class TheActivityUpdate : Update<TheActivityModel, TheActivityEvent, TheActivityEffect> {

  override fun update(model: TheActivityModel, event: TheActivityEvent): Next<TheActivityModel, TheActivityEffect> {
    return when (event) {
      is InitialScreenInfoLoaded -> decideInitialScreen(event)
      UserWasJustVerified -> dispatch(ShowUserLoggedOutOnOtherDeviceAlert)
      UserWasUnauthorized -> dispatch(RedirectToLoginScreen)
      UserWasDisapproved -> dispatch(ClearPatientData)
      PatientDataCleared -> dispatch(ShowAccessDeniedScreen)
    }
  }

  private fun decideInitialScreen(event: InitialScreenInfoLoaded): Next<TheActivityModel, TheActivityEffect> {
    val effect = initialScreenEffect(event.user)

    return dispatch(effect)
  }

  private fun initialScreenEffect(user: User): TheActivityEffect {
    val userDisapproved = user.status == UserStatus.DisapprovedForSyncing

    val canMoveToHomeScreen = when (user.loggedInStatus) {
      LoggedInStatus.RESETTING_PIN -> false
      LoggedInStatus.LOGGED_IN, LoggedInStatus.OTP_REQUESTED, LoggedInStatus.RESET_PIN_REQUESTED, LoggedInStatus.UNAUTHORIZED -> true
    }

    return when {
      canMoveToHomeScreen && !userDisapproved -> ShowHomeScreen
      userDisapproved -> ShowAccessDeniedScreen
      user.loggedInStatus == LoggedInStatus.RESETTING_PIN -> ShowForgotPinCreatePinScreen
      else -> throw IllegalStateException("Unknown user status combinations: [${user.loggedInStatus}, ${user.status}]")
    }
  }

  private fun screenLockEffect(
      currentTimestamp: Instant,
      lockAtTimestamp: Optional<Instant>,
      user: User
  ): TheActivityEffect {
    val hasAppLockTimerExpired = lockAtTimestamp
        .map(currentTimestamp::isAfter)
        .orElse(true) // Handle the case where the app is opened after a cold start

    val shouldShowAppLockScreen = shouldShowAppLockScreenForUser(user) && hasAppLockTimerExpired

    return if (shouldShowAppLockScreen) ShowAppLockScreen else ClearLockAfterTimestamp
  }

  private fun shouldShowAppLockScreenForUser(
      user: User
  ): Boolean {
    return user.isNotDisapprovedForSyncing && user.loggedInStatus in SHOW_APP_LOCK_FOR_USER_STATES
  }
}
