package org.simple.clinic.login.applock

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
data class AppLockScreenKey(
    val nextScreen: ScreenKey
) : ScreenKey(), Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "App Lock"

  override fun instantiateFragment() = AppLockScreen()
}
