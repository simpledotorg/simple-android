package org.simple.clinic.login.applock

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.History
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
data class AppLockScreenKey(
    val screenHistory: History,
    override val analyticsName: String = "App Lock"
) : ScreenKey(), Parcelable {

  override fun instantiateFragment() = AppLockScreen()
}
