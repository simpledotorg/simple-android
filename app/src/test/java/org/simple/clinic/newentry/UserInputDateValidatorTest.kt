package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RunWith(JUnitParamsRunner::class)
class UserInputDateValidatorTest {

  private val validator = UserInputDateValidator(
      userClock = TestUserClock(LocalDate.parse("2018-01-01")),
      dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))

  @Test
  @Parameters(method = "params for v2 validation")
  fun `validate (v2)`(date: String, expectedResult: UserInputDateValidator.Result) {
    assertThat(validator.validate(date)).isEqualTo(expectedResult)
  }

  @Suppress("unused")
  fun `params for v2 validation`(): List<Any> {
    return listOf(
        listOf("24/04/1971", Valid(LocalDate.of(1971, 4, 24))),
        listOf("24-04-1971", InvalidPattern),
        listOf("1971-04-24", InvalidPattern),
        listOf("1971-24-04", InvalidPattern),
        listOf("24/04", InvalidPattern),
        listOf(" ", InvalidPattern))
  }

  @Test
  fun `validate future date (v2)`() {
    val futureDateResult = validator.validate("01/01/3000", nowDate = LocalDate.parse("2018-07-16"))

    assertThat(futureDateResult).isEqualTo(DateIsInFuture)
  }
}
