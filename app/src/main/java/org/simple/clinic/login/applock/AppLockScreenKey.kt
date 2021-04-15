package org.simple.clinic.login.applock

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey

@Parcelize
object AppLockScreenKey : ScreenKey(), Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "App Lock"

  override fun instantiateFragment() = AppLockScreen()
}
