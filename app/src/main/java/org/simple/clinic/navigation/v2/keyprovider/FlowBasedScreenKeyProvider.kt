package org.simple.clinic.navigation.v2.keyprovider

import android.view.View
import org.simple.clinic.router.screen.ScreenRouter
import javax.inject.Inject

class FlowBasedScreenKeyProvider @Inject constructor(
    private val router: ScreenRouter
): ScreenKeyProvider {

  override fun <T> keyFor(view: View) = router.key<T>(view)
}
