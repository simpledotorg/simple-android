package org.simple.clinic.widgets.ageanddateofbirth

import androidx.annotation.VisibleForTesting
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
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
    return try {
      val parsedDate = dateOfBirthFormat.parse(dateText, LocalDate::from)
      when {
        parsedDate > nowDate -> DateIsInFuture
        else -> Result.Valid(parsedDate)
      }
    } catch (dte: DateTimeParseException) {
      InvalidPattern
    }
  }

  @VisibleForTesting
  fun dateInUserTimeZone(): LocalDate {
    return LocalDate.now(userTimeZone)
  }
}
