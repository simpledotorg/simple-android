package org.simple.clinic.login.pin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.user.OngoingLoginEntry

@Parcelize
data class LoginPinModel(val ongoingLoginEntry: OngoingLoginEntry?) : Parcelable {

  companion object {
    fun create() = LoginPinModel(null)
  }

  val hasOngoingLoginEntry: Boolean
    get() = ongoingLoginEntry != null

  fun ongoingLoginEntryUpdated(ongoingLoginEntry: OngoingLoginEntry): LoginPinModel {
    return copy(ongoingLoginEntry = ongoingLoginEntry)
  }
}
