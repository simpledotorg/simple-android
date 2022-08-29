package org.simple.clinic.navigation.v2.keyprovider

import android.view.View

/**
 * Helper interface added to abstract the source for the screen key in screens, whether it be
 * [org.simple.clinic.navigation.v2.Router] or something else we add in the future.
 **/
interface ScreenKeyProvider {
  fun <T> keyFor(view: View): T
}
