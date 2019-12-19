package org.simple.clinic.widgets.ageanddateofbirth

import androidx.annotation.VisibleForTesting
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.AgeIsInvalid
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.DateIsInvalid
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Valid.AgeIsValid
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Valid.DateIsValid
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named


class UserInputAgeValidator @Inject constructor(
        private val userClock: UserClock,
        @Named("date_for_user_input") private val dateOfBirthFormat: DateTimeFormatter
) {

    sealed class Result {
        sealed class Valid : Result() {
            data class AgeIsValid(val age: Int) : Valid()
            data class DateIsValid(val parsedDate: LocalDate) : Valid()
        }

        sealed class Invalid : Result() {
            object AgeIsInvalid : Invalid()
            object DateIsInvalid : Invalid()
        }
    }


    fun invalidAgeValidator(age: Int): Result {
        return when {
            age > 120 -> AgeIsInvalid
            else -> AgeIsValid(age)
        }
    }

    fun invalidDateValidator(dateText: String, nowDate: LocalDate = dateInUserTimeZone()): Result {
        val parsedDate = dateOfBirthFormat.parse(dateText, LocalDate::from)
        return when {
            parsedDate < nowDate.minusYears(120) -> DateIsInvalid
            else -> DateIsValid(parsedDate)
        }
    }

    @VisibleForTesting
    fun dateInUserTimeZone(): LocalDate {
        return LocalDate.now(userClock)
    }
}