package org.resolvetosavelives.red.newentry

import org.resolvetosavelives.red.util.LocalDateRoomTypeConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeParseException
import javax.inject.Inject

class DateOfBirthFormatValidator @Inject constructor() {

  enum class Result {
    VALID,
    INVALID
  }

  fun validate(dateText: String): Result {
    return try {
      LocalDateRoomTypeConverter.formatter.parse(dateText, LocalDate::from)
      Result.VALID

    } catch (e: Exception) {
      when (e) {
        is NullPointerException -> Result.INVALID
        is DateTimeParseException -> Result.INVALID
        else -> throw e
      }
    }
  }
}
