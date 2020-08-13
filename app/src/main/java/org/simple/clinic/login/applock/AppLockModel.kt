package org.simple.clinic.login.applock

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AppLockModel : Parcelable {

  companion object {
    fun create() = AppLockModel()
  }
}
