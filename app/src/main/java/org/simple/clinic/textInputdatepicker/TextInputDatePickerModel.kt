package org.simple.clinic.textInputdatepicker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class TextInputDatePickerModel(
  val day: String,
  val month: String,
  val year: String,
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val prefilledDate: LocalDate?
) : Parcelable {

  companion object {
    fun create(minDate: LocalDate, maxDate: LocalDate, prefilledDate: LocalDate?) = TextInputDatePickerModel(
      day = "",
      month = "",
      year = "",
      minDate = minDate,
      maxDate = maxDate,
      prefilledDate = prefilledDate
    )
  }

  fun dayChanged(day: String) = copy(day = day)

  fun monthChanged(month: String) = copy(month = month)

  fun yearChanged(year: String) = copy(year = year)

  fun prefilledDate(prefilledDate: LocalDate?) = copy(prefilledDate = prefilledDate)
}
