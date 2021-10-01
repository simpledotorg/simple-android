package org.simple.clinic.main

import org.simple.clinic.navigation.v2.History
import org.simple.clinic.navigation.v2.ScreenKey

interface TheActivityUiActions {
  // This is here because we need to show the same alert in multiple
  // screens when the user gets verified in the background.
  fun showUserLoggedOutOnOtherDeviceAlert()
  fun redirectToLogin()
  fun showAccessDeniedScreen(fullName: String)
  fun showInitialScreen(screenKey: ScreenKey)
  fun setCurrentScreenHistory(newHistory: History)
}
