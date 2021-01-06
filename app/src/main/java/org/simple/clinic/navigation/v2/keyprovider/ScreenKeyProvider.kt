package org.simple.clinic.navigation.v2.keyprovider

import android.view.View

/**
 * Helper interface added to abstract the source for the screen key in screens, whether it be
 * [org.simple.clinic.router.screen.ScreenRouter] or [org.simple.clinic.navigation.v2.Router].
 **/
interface ScreenKeyProvider {
  fun <T> keyFor(view: View): T
}
