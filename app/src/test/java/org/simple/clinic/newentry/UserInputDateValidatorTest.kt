package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result2.Invalid
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result2.Valid
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@RunWith(JUnitParamsRunner::class)
class UserInputDateValidatorTest {

  private val validator = UserInputDateValidator(
      userTimeZone = UTC,
      dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))

  @Test
  @Parameters(method = "params for v2 validation")
  fun `validate (v2)`(date: String, expectedResult: UserInputDateValidator.Result2) {
    assertThat(validator.validate2(date)).isEqualTo(expectedResult)
  }

  @Suppress("unused")
  fun `params for v2 validation`(): List<Any> {
    return listOf(
        listOf("24/04/1971", Valid(LocalDate.of(1971, 4, 24))),
        listOf("24-04-1971", Invalid.InvalidPattern),
        listOf("1971-04-24", Invalid.InvalidPattern),
        listOf("1971-24-04", Invalid.InvalidPattern),
        listOf("24/04", Invalid.InvalidPattern),
        listOf(" ", Invalid.InvalidPattern))
  }

  @Test
  fun `validate future date (v2)`() {
    val futureDateResult = validator.validate2("01/01/3000", nowDate = LocalDate.parse("2018-07-16"))

    assertThat(futureDateResult).isEqualTo(Invalid.DateIsInFuture)
  }
}
