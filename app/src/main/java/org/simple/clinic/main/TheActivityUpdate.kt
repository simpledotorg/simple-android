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
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.UNAUTHORIZED
import java.time.Instant
import java.util.Optional

private val SHOW_APP_LOCK_FOR_USER_STATES = setOf(
    OTP_REQUESTED,
    LOGGED_IN,
    RESET_PIN_REQUESTED
)

class TheActivityUpdate : Update<TheActivityModel, TheActivityEvent, TheActivityEffect> {

  override fun update(
      model: TheActivityModel,
      event: TheActivityEvent
  ): Next<TheActivityModel, TheActivityEffect> {
    return when (event) {
      is InitialScreenInfoLoaded -> decideInitialScreen(
          model = model,
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
      model: TheActivityModel,
      currentTimestamp: Instant,
      lockAtTimestamp: Optional<Instant>,
      user: User
  ): Next<TheActivityModel, TheActivityEffect> {
    val hasAppLockTimerExpired = lockAtTimestamp
        .map(currentTimestamp::isAfter)
        .orElse(true) // Handle the case where the app is opened after a cold start

    val shouldShowAppLockScreen = shouldShowAppLockScreenForUser(user) && hasAppLockTimerExpired

    val canMoveToHomeScreen = when (user.loggedInStatus) {
      RESETTING_PIN -> false
      LOGGED_IN, OTP_REQUESTED, RESET_PIN_REQUESTED, UNAUTHORIZED -> true
    }

    val initialScreen = when {
      user.isDisapprovedForSyncing -> AccessDeniedScreenKey(user.fullName)
      canMoveToHomeScreen && user.isNotDisapprovedForSyncing -> HomeScreenKey
      user.isResettingPin -> ForgotPinCreateNewPinScreenKey().wrap()
      else -> throw IllegalStateException("Unknown user status combinations: [${user.loggedInStatus}, ${user.status}]")
    }

    return if (shouldShowAppLockScreen && !model.isFreshLogin)
      dispatch(ShowInitialScreen(AppLockScreenKey(initialScreen)))
    else
      dispatch(ShowInitialScreen(initialScreen), ClearLockAfterTimestamp)
  }

  private fun shouldShowAppLockScreenForUser(
      user: User
  ): Boolean {
    return user.isNotDisapprovedForSyncing && user.loggedInStatus in SHOW_APP_LOCK_FOR_USER_STATES
  }
}
