package org.simple.clinic.textInputdatepicker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TextInputDatePickerModel(
    val day: String,
    val month: String
) : Parcelable {

  companion object {
    fun create() = TextInputDatePickerModel(
        day = "",
        month = ""
    )
  }

  fun dayChanged(day: String) = copy(day = day)

  fun monthChanged(month: String) = copy(month = month)
}
