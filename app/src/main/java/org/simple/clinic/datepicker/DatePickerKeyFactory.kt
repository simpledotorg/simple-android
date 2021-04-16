package org.simple.clinic.datepicker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.appconfig.Country
import org.simple.clinic.datepicker.calendar.CalendarDatePicker
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.textInputdatepicker.TextInputDatePickerSheet
import java.time.LocalDate
import javax.inject.Inject

class DatePickerKeyFactory @Inject constructor(
    private val country: Country
) {

  fun key(
      preselectedDate: LocalDate,
      allowedDateRange: ClosedRange<LocalDate>
  ): ScreenKey {
    return when (country.isoCountryCode) {
      Country.BANGLADESH, Country.INDIA -> showCalendarDatePicker(preselectedDate, allowedDateRange)
      Country.ETHIOPIA -> showTextInputDatePicker(preselectedDate, allowedDateRange)
      else -> showCalendarDatePicker(preselectedDate, allowedDateRange)
    }
  }

  private fun showTextInputDatePicker(preselectedDate: LocalDate, allowedDateRange: ClosedRange<LocalDate>): ScreenKey {
    return TextInputDatePickerSheet.Key(prefilledDate = preselectedDate,
        minDate = allowedDateRange.start,
        maxDate = allowedDateRange.endInclusive
    )
  }

  private fun showCalendarDatePicker(preselectedDate: LocalDate, allowedDateRange: ClosedRange<LocalDate>): ScreenKey {
    return CalendarDatePicker.Key(preselectedDate = preselectedDate,
        minDate = allowedDateRange.start,
        maxDate = allowedDateRange.endInclusive)
  }
}

@Parcelize
object DatePickerResult : Parcelable

@Parcelize
data class SelectedDate(val date: LocalDate) : Parcelable
