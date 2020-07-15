package org.simple.clinic.summary.addphone

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AddPhoneNumberModel : Parcelable {

  companion object {
    fun create() = AddPhoneNumberModel()
  }
}
