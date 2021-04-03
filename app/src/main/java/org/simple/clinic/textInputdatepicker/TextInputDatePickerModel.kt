package org.simple.clinic.textInputdatepicker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TextInputDatePickerModel(
    val day: String
) : Parcelable {

  companion object {
    fun create() = TextInputDatePickerModel(
        day = ""
    )
  }

  fun dayChanged(day: String) = copy(day = day)
}
