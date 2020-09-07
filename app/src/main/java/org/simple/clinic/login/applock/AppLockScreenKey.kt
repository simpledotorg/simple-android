package org.simple.clinic.login.applock

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
object AppLockScreenKey : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "App Lock"

  override fun layoutRes() = R.layout.screen_app_lock
}
