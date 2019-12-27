package org.simple.clinic.widgets.ageanddateofbirth

import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid
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
    object Invalid : Result()
  }

  fun validate(age: Int): Result {
    return when (age > 120) {
      true -> Invalid
      false -> Valid
    }
  }

  fun validate(dateText: String): Result {
    val nowDate: LocalDate = LocalDate.now(userClock)
    val parsedDate = dateOfBirthFormat.parse(dateText, LocalDate::from)
    return when {
      parsedDate < nowDate.minusYears(120) -> Invalid
      else -> Valid
    }
  }
}