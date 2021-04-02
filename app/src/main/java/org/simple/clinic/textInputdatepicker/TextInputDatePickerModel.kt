package org.simple.clinic.textInputdatepicker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class TextInputDatePickerModel : Parcelable {

  companion object {
    fun create() = TextInputDatePickerModel()
  }
}
