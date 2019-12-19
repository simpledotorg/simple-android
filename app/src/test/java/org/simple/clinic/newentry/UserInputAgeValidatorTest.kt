package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.AgeIsInvalid
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.DateIsInvalid
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Valid.AgeIsValid
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Valid.DateIsValid
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class UserInputAgeValidatorTest {
    @Inject
    private val validator = UserInputAgeValidator(userClock = TestUserClock(LocalDate.parse("2018-01-01")),
            dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))

    @Test
    fun `when user inputs age greater than 120 years, then return invalid error`() {
        //given
        val age = 123

        //when
        val expectedResult = validator.invalidAgeValidator(age)

        //then
        assertThat(expectedResult).isEqualTo(AgeIsInvalid)
    }

    @Test
    fun `when user inputs age less than 120 years, then return valid`() {
        //given
        val age = 98

        //when
        val expectedResult = validator.invalidAgeValidator(age)

        //then
        assertThat(expectedResult).isEqualTo(AgeIsValid(age))
    }

    @Test
    fun `when user inputs age equal to 120 years, then return valid`() {
        //given
        val age = 120

        //when
        val expectedResult = validator.invalidAgeValidator(age)

        //then
        assertThat(expectedResult).isEqualTo(AgeIsValid(age))
    }

    @Test
    fun `when user inputs date greater than 120 years, return invalid`() {
        //given
        val dateText = "12/03/1810"

        //when
        val validation = validator.invalidDateValidator(dateText)

        //then
        assertThat(validation).isEqualTo(DateIsInvalid)
    }

    @Test
    fun `when user inputs date less than 120 years, then return valid`() {
        //given
        val dateText = "15/12/2019"

        //when
        val validation = validator.invalidDateValidator(dateText)

        //then
        assertThat(validation).isEqualTo(DateIsValid(LocalDate.of(2019, 12, 15)))
    }

    @Test
    fun `when user inputs date equal to 120 years, then return valid`() {
        //given
        val dateText = "17/12/1899"

        //when
        val validation = validator.invalidDateValidator(dateText)

        //then
        assertThat(validation).isEqualTo(DateIsValid(LocalDate.of(1899, 12, 17)))
    }


}