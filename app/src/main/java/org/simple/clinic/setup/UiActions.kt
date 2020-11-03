package org.simple.clinic.setup

import org.simple.clinic.setup.runcheck.Disallowed

interface UiActions {
  fun goToMainActivity()

  fun showSplashScreen()

  fun showCountrySelectionScreen()

  fun showDisallowedToRunError(reason: Disallowed.Reason)
}
