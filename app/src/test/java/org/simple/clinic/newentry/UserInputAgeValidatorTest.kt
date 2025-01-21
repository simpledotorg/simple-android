package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.ExceedsMaxAgeLimit
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.ExceedsMinAgeLimit
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Valid
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class UserInputAgeValidatorTest {

  private val testUserClock = TestUserClock(LocalDate.parse("2020-01-01"))

  private lateinit var validator: UserInputAgeValidator

  @Before
  fun setUp() {
    validator = UserInputAgeValidator(
        userClock = testUserClock,
        dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
    )
  }

  @Test
  fun `when user inputs age greater than 120 years, then return invalid error`() {
    //given
    val age = 123

    //when
    val expectedResult = validator.validate(age)

    //then
    assertThat(expectedResult).isEqualTo(ExceedsMaxAgeLimit)
  }

  @Test
  fun `when user inputs age less than 120 years, then return valid`() {
    //given
    val age = 98

    //when
    val expectedResult = validator.validate(age)

    //then
    assertThat(expectedResult).isEqualTo(Valid)
  }

  @Test
  fun `when user inputs age equal to 120 years, then return valid`() {
    //given
    val age = 120

    //when
    val expectedResult = validator.validate(age)

    //then
    assertThat(expectedResult).isEqualTo(Valid)
  }

  @Test
  fun `when user inputs date greater than 120 years, return invalid`() {
    //given
    val dateText = "12/03/1810"

    //when
    val validation = validator.validate(dateText)

    //then
    assertThat(validation).isEqualTo(ExceedsMaxAgeLimit)
  }

  @Test
  fun `when user inputs date less than 120 years, then return valid`() {
    //given
    val dateText = "15/12/2019"

    //when
    val validation = validator.validate(dateText)

    //then
    assertThat(validation).isEqualTo(Valid)
  }

  @Test
  fun `when user inputs date equal to 120 years, then return valid`() {
    //given
    val dateText = "01/01/1900"

    //when
    val validation = validator.validate(dateText)

    //then
    assertThat(validation).isEqualTo(Valid)
  }

  @Test
  fun `when user inputs age equal to 0 years, then return invalid error`() {
    //given
    val age = 0

    //when
    val expectedResult = validator.validate(age)

    //then
    assertThat(expectedResult).isEqualTo(ExceedsMinAgeLimit)
  }

  @Test
  fun `when user inputs current date, then return invalid error`() {
    //given
    val dateText = "01/01/2020"

    //when
    val expectedResult = validator.validate(dateText)

    //then
    assertThat(expectedResult).isEqualTo(ExceedsMinAgeLimit)
  }
}
