package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.IsInvalid
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.IsValid
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

class UserInputAgeValidatorTest {

  private val testUserClock = TestUserClock()

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
    val expectedResult = validator.validator(age)

    //then
    assertThat(expectedResult).isEqualTo(IsInvalid)
  }

  @Test
  fun `when user inputs age less than 120 years, then return valid`() {
    //given
    val age = 98

    //when
    val expectedResult = validator.validator(age)

    //then
    assertThat(expectedResult).isEqualTo(IsValid)
  }

  @Test
  fun `when user inputs age equal to 120 years, then return valid`() {
    //given
    val age = 120

    //when
    val expectedResult = validator.validator(age)

    //then
    assertThat(expectedResult).isEqualTo(IsValid)
  }

  @Test
  fun `when user inputs date greater than 120 years, return invalid`() {
    //given
    val dateText = "12/03/1810"

    //when
    val validation = validator.validator(dateText)

    //then
    assertThat(validation).isEqualTo(IsInvalid)
  }

  @Test
  fun `when user inputs date less than 120 years, then return valid`() {
    //given
    val dateText = "15/12/2019"

    //when
    val validation = validator.validator(dateText)

    //then
    assertThat(validation).isEqualTo(IsValid)
  }

  @Test
  fun `when user inputs date equal to 120 years, then return valid`() {
    //given
    val dateText = "17/12/1899"

    //when
    val validation = validator.validator(dateText)

    //then
    assertThat(validation).isEqualTo(IsValid)
  }
}