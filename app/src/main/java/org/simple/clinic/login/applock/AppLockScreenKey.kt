package org.simple.clinic.login.applock

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class AppLockScreenKey : FullScreenKey, Parcelable {

  override fun layoutRes() = R.layout.screen_app_lock
}
