package org.simple.clinic.enterotp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class EnterOtpModel: Parcelable {

  companion object {

    fun create(): EnterOtpModel {
      return EnterOtpModel()
    }
  }
}
