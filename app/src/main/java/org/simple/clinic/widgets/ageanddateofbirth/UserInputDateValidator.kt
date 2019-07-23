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

  sealed class Result2 {
    data class Valid(val parsedDate: LocalDate) : Result2()
    sealed class Invalid : Result2() {
      object InvalidPattern : Invalid()
      object DateIsInFuture : Invalid()
    }
  }

  fun validate2(dateText: String, nowDate: LocalDate = dateInUserTimeZone()): Result2 {
    try {
      if (dateText.isBlank()) {
        return Result2.Invalid.InvalidPattern
      }

      val parsedDate = dateOfBirthFormat.parse(dateText, LocalDate::from)
      return when {
        parsedDate > nowDate -> Result2.Invalid.DateIsInFuture
        else -> Result2.Valid(parsedDate)
      }

    } catch (e: Exception) {
      return when (e) {
        is DateTimeParseException -> Result2.Invalid.InvalidPattern
        else -> throw e
      }
    }
  }

  @VisibleForTesting
  fun dateInUserTimeZone(): LocalDate {
    return LocalDate.now(userTimeZone)
  }
}
