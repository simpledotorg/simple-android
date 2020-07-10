package org.simple.clinic.widgets.ageanddateofbirth

import org.simple.clinic.MAX_ALLOWED_PATIENT_AGE
import org.simple.clinic.MIN_ALLOWED_PATIENT_AGE
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.ExceedsMaxAgeLimit
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.ExceedsMinAgeLimit
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Valid
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class UserInputAgeValidator @Inject constructor(
    private val userClock: UserClock,
    @Named("date_for_user_input") private val dateOfBirthFormat: DateTimeFormatter
) {
  sealed class Result {
    object Valid : Result()
    sealed class Invalid : Result() {
      object ExceedsMaxAgeLimit : Invalid()
      object ExceedsMinAgeLimit : Invalid()
    }
  }

  fun validate(age: Int): Result {
    return when {
      age > MAX_ALLOWED_PATIENT_AGE -> ExceedsMaxAgeLimit
      age < MIN_ALLOWED_PATIENT_AGE -> ExceedsMinAgeLimit
      else -> Valid
    }
  }

  fun validate(dateText: String): Result {
    val nowDate: LocalDate = LocalDate.now(userClock)
    val parsedDate = dateOfBirthFormat.parse(dateText, LocalDate::from)
    return when {
      parsedDate < nowDate.minusYears(MAX_ALLOWED_PATIENT_AGE.toLong()) -> ExceedsMaxAgeLimit
      parsedDate == nowDate -> ExceedsMinAgeLimit
      else -> Valid
    }
  }
}
