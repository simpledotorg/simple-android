package org.simple.clinic.main

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.deniedaccess.AccessDeniedScreenKey
import org.simple.clinic.forgotpin.createnewpin.ForgotPinCreateNewPinScreenKey
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.login.applock.AppLockScreenKey
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus
import org.simple.clinic.user.UserStatus
import java.time.Instant
import java.util.Optional

private val SHOW_APP_LOCK_FOR_USER_STATES = setOf(
    LoggedInStatus.OTP_REQUESTED,
    LoggedInStatus.LOGGED_IN,
    LoggedInStatus.RESET_PIN_REQUESTED
)

class TheActivityUpdate : Update<TheActivityModel, TheActivityEvent, TheActivityEffect> {

  override fun update(
      model: TheActivityModel,
      event: TheActivityEvent
  ): Next<TheActivityModel, TheActivityEffect> {
    return when (event) {
      is InitialScreenInfoLoaded -> decideInitialScreen(
          currentTimestamp = event.currentTimestamp,
          lockAtTimestamp = event.lockAtTimestamp,
          user = event.user
      )
      UserWasJustVerified -> dispatch(ShowUserLoggedOutOnOtherDeviceAlert)
      UserWasUnauthorized -> dispatch(RedirectToLoginScreen)
      UserWasDisapproved -> dispatch(ClearPatientData)
      PatientDataCleared -> dispatch(ShowAccessDeniedScreen)
    }
  }

  private fun decideInitialScreen(
      currentTimestamp: Instant,
      lockAtTimestamp: Optional<Instant>,
      user: User
  ): Next<TheActivityModel, TheActivityEffect> {
    val hasAppLockTimerExpired = lockAtTimestamp
        .map(currentTimestamp::isAfter)
        .orElse(true) // Handle the case where the app is opened after a cold start

    val shouldShowAppLockScreen = shouldShowAppLockScreenForUser(user) && hasAppLockTimerExpired
    val userDisapproved = user.status == UserStatus.DisapprovedForSyncing

    val canMoveToHomeScreen = when (user.loggedInStatus) {
      LoggedInStatus.RESETTING_PIN -> false
      LoggedInStatus.LOGGED_IN, LoggedInStatus.OTP_REQUESTED, LoggedInStatus.RESET_PIN_REQUESTED, LoggedInStatus.UNAUTHORIZED -> true
    }

    val initialScreen = when {
      shouldShowAppLockScreen -> AppLockScreenKey()
      userDisapproved -> AccessDeniedScreenKey(user.fullName)
      canMoveToHomeScreen && !userDisapproved -> HomeScreenKey
      user.loggedInStatus == LoggedInStatus.RESETTING_PIN -> ForgotPinCreateNewPinScreenKey().wrap()
      else -> throw IllegalStateException("Unknown user status combinations: [${user.loggedInStatus}, ${user.status}]")
    }

    return if (shouldShowAppLockScreen) dispatch(ShowInitialScreen(initialScreen)) else dispatch(ShowInitialScreen(initialScreen), ClearLockAfterTimestamp)
  }

  private fun shouldShowAppLockScreenForUser(
      user: User
  ): Boolean {
    return user.isNotDisapprovedForSyncing && user.loggedInStatus in SHOW_APP_LOCK_FOR_USER_STATES
  }
}
