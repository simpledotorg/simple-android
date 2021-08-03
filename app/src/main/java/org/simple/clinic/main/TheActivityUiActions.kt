package org.simple.clinic.main

import org.simple.clinic.navigation.v2.ScreenKey

interface TheActivityUiActions {
  fun showAppLockScreen()

  // This is here because we need to show the same alert in multiple
  // screens when the user gets verified in the background.
  fun showUserLoggedOutOnOtherDeviceAlert()
  fun redirectToLogin()
  fun showAccessDeniedScreen(fullName: String)
  fun showHomeScreen()
  fun showForgotPinScreen()
  fun showInitialScreen(screenKey: ScreenKey)
}
