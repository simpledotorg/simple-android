package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.AgeIsInvalid
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Valid.AgeIsValid

class UserInputAgeValidatorTest {
    private val validator = UserInputAgeValidator()

    @Test
    fun `when user inputs age greater than 120 years, then return invalid error`() {
        //given
        val age = 123

        //when
        val expectedResult = validator.ageValidator(age)

        //then
        assertThat(expectedResult).isEqualTo(AgeIsInvalid)
    }

    @Test
    fun `when user inputs age less than 120 years, then return valid`() {
        //given
        val age = 98

        //when
        val expectedResult = validator.ageValidator(age)

        //then
        assertThat(expectedResult).isEqualTo(AgeIsValid(age))
    }

    @Test
    fun `when user inputs age equal to 120 years, then return valid`() {
        //given
        val age = 120

        //when
        val expectedResult = validator.ageValidator(age)

        //then
        assertThat(expectedResult).isEqualTo(AgeIsValid(age))
    }


}