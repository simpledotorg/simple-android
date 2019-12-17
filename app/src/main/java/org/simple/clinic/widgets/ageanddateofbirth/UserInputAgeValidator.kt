package org.simple.clinic.widgets.ageanddateofbirth

import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.AgeIsInvalid
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Valid.AgeIsValid

class UserInputAgeValidator {

    sealed class Result {
        sealed class Valid : Result() {
            data class AgeIsValid(val age: Int) : Valid()
        }

        sealed class Invalid : Result() {
            object AgeIsInvalid : Invalid()
        }
    }


    fun ageValidator(age: Int): Result {
        return when {
            age > 120 -> AgeIsInvalid
            else -> AgeIsValid(age)
        }
    }

}