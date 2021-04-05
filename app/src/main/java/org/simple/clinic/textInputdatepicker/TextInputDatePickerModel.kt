package org.simple.clinic.textInputdatepicker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate

@Parcelize
data class TextInputDatePickerModel(
    val day: String,
    val month: String,
    val year: String,
    val minDate: LocalDate,
    val maxDate: LocalDate
) : Parcelable {

  companion object {
    fun create(minDate: LocalDate, maxDate: LocalDate) = TextInputDatePickerModel(
        day = "",
        month = "",
        year = "",
        minDate = minDate,
        maxDate = maxDate
    )
  }

  fun dayChanged(day: String) = copy(day = day)

  fun monthChanged(month: String) = copy(month = month)

  fun yearChanged(year: String) = copy(year = year)
}
