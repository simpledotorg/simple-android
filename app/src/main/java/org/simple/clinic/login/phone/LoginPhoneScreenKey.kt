package org.simple.clinic.login.phone

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class LoginPhoneScreenKey(val otp: String) : FullScreenKey, Parcelable {

  override fun layoutRes() = R.layout.screen_login_phone
}
