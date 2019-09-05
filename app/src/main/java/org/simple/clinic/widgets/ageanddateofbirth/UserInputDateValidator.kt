package org.simple.clinic.widgets.ageanddateofbirth

import androidx.annotation.VisibleForTesting
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Named

class UserInputDateValidator @Inject constructor(
    val userTimeZone: ZoneId,
    @Named("date_for_user_input") private val dateOfBirthFormat: DateTimeFormatter
) {

  sealed class Result {
    data class Valid(val parsedDate: LocalDate) : Result()
    sealed class Invalid : Result() {
      object InvalidPattern : Invalid()
      object DateIsInFuture : Invalid()
    }
  }

  fun validate(dateText: String, nowDate: LocalDate = dateInUserTimeZone()): Result {
    try {
      if (dateText.isBlank()) {
        return Result.Invalid.InvalidPattern
      }

      val parsedDate = dateOfBirthFormat.parse(dateText, LocalDate::from)
      return when {
        parsedDate > nowDate -> Result.Invalid.DateIsInFuture
        else -> Result.Valid(parsedDate)
      }
    } catch (dte: DateTimeParseException) {
      return Result.Invalid.InvalidPattern
    }
  }

  @VisibleForTesting
  fun dateInUserTimeZone(): LocalDate {
    return LocalDate.now(userTimeZone)
  }
}
