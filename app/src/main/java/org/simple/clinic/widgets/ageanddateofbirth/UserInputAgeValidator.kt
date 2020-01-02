package org.simple.clinic.widgets.ageanddateofbirth

import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.ExceedsMaxAge
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.ExceedsMinAge
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Valid
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class UserInputAgeValidator @Inject constructor(
    private val userClock: UserClock,
    @Named("date_for_user_input") private val dateOfBirthFormat: DateTimeFormatter
) {
  sealed class Result {
    object Valid : Result()
    sealed class Invalid : Result() {
      object ExceedsMaxAge : Invalid()
      object ExceedsMinAge : Invalid()
    }
  }

  fun validate(age: Int): Result {
    return when {
      age > 120 -> ExceedsMaxAge
      age == 0 -> ExceedsMinAge
      else -> Valid
    }
  }

  fun validate(dateText: String): Result {
    val nowDate: LocalDate = LocalDate.now(userClock)
    val parsedDate = dateOfBirthFormat.parse(dateText, LocalDate::from)
    return when {
      parsedDate < nowDate.minusYears(120) -> ExceedsMaxAge
      parsedDate == nowDate -> ExceedsMinAge
      else -> Valid
    }
  }
}
