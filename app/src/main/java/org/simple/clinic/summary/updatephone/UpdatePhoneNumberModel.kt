package org.simple.clinic.summary.updatephone

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UpdatePhoneNumberModel : Parcelable {

  companion object {
    fun create() = UpdatePhoneNumberModel()
  }
}
