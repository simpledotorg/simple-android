package org.simple.clinic.datepicker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.appconfig.Country
import org.simple.clinic.datepicker.calendar.CalendarDatePicker
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.textInputdatepicker.TextInputDatePickerSheet
import java.time.LocalDate
import javax.inject.Inject

class DatePickerKeyFactory @Inject constructor(
    private val country: Country,
    private val features: Features
) {

  fun key(
      preselectedDate: LocalDate,
      allowedDateRange: ClosedRange<LocalDate>
  ): ScreenKey {
    if (features.isDisabled(Feature.EthiopianCalendar)) {
      return showCalendarDatePicker(
          preselectedDate = preselectedDate,
          allowedDateRange = allowedDateRange
      )
    }

    return when (country.isoCountryCode) {
      Country.ETHIOPIA -> showTextInputDatePicker(preselectedDate, allowedDateRange)
      else -> showCalendarDatePicker(preselectedDate, allowedDateRange)
    }
  }

  private fun showTextInputDatePicker(
      preselectedDate: LocalDate,
      allowedDateRange: ClosedRange<LocalDate>
  ): ScreenKey {
    return TextInputDatePickerSheet.Key(prefilledDate = preselectedDate,
        minDate = allowedDateRange.start,
        maxDate = allowedDateRange.endInclusive
    )
  }

  private fun showCalendarDatePicker(
      preselectedDate: LocalDate,
      allowedDateRange: ClosedRange<LocalDate>
  ): ScreenKey {
    return CalendarDatePicker.Key(preselectedDate = preselectedDate,
        minDate = allowedDateRange.start,
        maxDate = allowedDateRange.endInclusive)
  }
}

@Parcelize
object DatePickerResult : Parcelable

@Parcelize
data class SelectedDate(val date: LocalDate) : Parcelable
