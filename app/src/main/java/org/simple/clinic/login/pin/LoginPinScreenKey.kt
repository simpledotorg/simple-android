package org.simple.clinic.login.pin

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class LoginPinScreenKey : FullScreenKey, Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "Login PIN Entry"

  override fun layoutRes() = R.layout.screen_login_pin

}
