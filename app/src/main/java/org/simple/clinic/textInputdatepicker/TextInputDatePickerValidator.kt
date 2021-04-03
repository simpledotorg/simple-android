package org.simple.clinic.textInputdatepicker

import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.NotValid.DateIsInPast
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.NotValid.InvalidPattern
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.NotValid.MaximumAllowedDateRange
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Named

class TextInputDatePickerValidator @Inject constructor(
    @Named("date_for_user_input") private val userEnteredDateFormat: DateTimeFormatter
) {

  sealed class Result {
    data class Valid(val parsedDate: LocalDate) : Result()
    sealed class NotValid : Result() {
      object InvalidPattern : NotValid()
      object DateIsInPast : NotValid()
      object MaximumAllowedDateRange : NotValid()
    }
  }

  fun validate(minDate: LocalDate, maxDate: LocalDate, dateText: String): Result {
    return try {
      val parsedDate = userEnteredDateFormat.parse(dateText, LocalDate::from)

      when {
        parsedDate.isAfter(maxDate) -> MaximumAllowedDateRange
        parsedDate.isBefore(minDate) -> DateIsInPast
        else -> Result.Valid(parsedDate)
      }
    } catch (dte: DateTimeParseException) {
      InvalidPattern
    }
  }
}
