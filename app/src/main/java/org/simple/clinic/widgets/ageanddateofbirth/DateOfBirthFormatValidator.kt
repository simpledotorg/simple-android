package org.simple.clinic.widgets.ageanddateofbirth

import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Named

class DateOfBirthFormatValidator @Inject constructor(
    @Named("date_for_user_input") private val dateOfBirthFormat: DateTimeFormatter
) {

  enum class Result {
    VALID,
    INVALID_PATTERN,
    DATE_IS_IN_FUTURE,
  }

  sealed class Result2 {
    data class Valid(val parsedDate: LocalDate) : Result2()
    sealed class Invalid : Result2() {
      object InvalidPattern : Invalid()
      object DateIsInFuture : Invalid()
    }
  }

  @Deprecated(
      message = "use validate2 instead which uses sealed classes instead of enums",
      replaceWith = ReplaceWith("validate2(dateText, nowDate)"))
  fun validate(dateText: String, nowDate: LocalDate = LocalDate.now(UTC)): Result {
    return when (validate2(dateText, nowDate)) {
      is Result2.Valid -> Result.VALID
      is Result2.Invalid.InvalidPattern -> Result.INVALID_PATTERN
      is Result2.Invalid.DateIsInFuture -> Result.DATE_IS_IN_FUTURE
    }
  }

  fun validate2(dateText: String, nowDate: LocalDate = LocalDate.now(UTC)): Result2 {
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
}
