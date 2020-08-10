package org.simple.clinic.registration.phone.loggedout

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class LoggedOutOfDeviceModel : Parcelable {

  companion object {
    fun create() = LoggedOutOfDeviceModel()
  }
}
