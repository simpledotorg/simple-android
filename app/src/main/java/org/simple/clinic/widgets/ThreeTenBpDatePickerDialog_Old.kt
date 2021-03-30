package org.simple.clinic.widgets

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toUtcInstant
import java.time.LocalDate

/**
 * An extension of [DatePickerDialog] that supports the ThreeTenBp
 * classes.
 *
 * The DatePickerDialog uses 0-based indices for the Month (0 is January, 1 is February...),
 * while LocalDate uses 1-based indices (1 is January, 2 is February...).
 *
 * So when we convert from LocalDate to the DatePicker (and vice versa), we have to adjust the
 * month accordingly.
 **/
class ThreeTenBpDatePickerDialog_Old(
    context: Context,
    preselectedDate: LocalDate,
    allowedDateRange: ClosedRange<LocalDate>,
    clock: UserClock,
    datePickedListener: (LocalDate) -> Unit
) : DatePickerDialog(
    context,
    OnDateSetListener { _, year, month, dayOfMonth ->
      val setDate = LocalDate.of(year, month + 1, dayOfMonth)
      datePickedListener.invoke(setDate)
    },
    preselectedDate.year,
    preselectedDate.monthValue - 1,
    preselectedDate.dayOfMonth
) {

  init {
    datePicker.apply {
      minDate = allowedDateRange.start.toUtcInstant(clock).toEpochMilli()
      maxDate = allowedDateRange.endInclusive.toUtcInstant(clock).toEpochMilli()
    }
  }
}
