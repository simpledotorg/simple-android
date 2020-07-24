package org.simple.clinic.login.pin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.OngoingLoginEntry

@Parcelize
data class LoginPinModel(val ongoingLoginEntry: OngoingLoginEntry?) : Parcelable {

  companion object {
    fun create() = LoginPinModel(null)
  }
}
