package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator.Result
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator.Result2.Invalid
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator.Result2.Valid
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@RunWith(JUnitParamsRunner::class)
class DateOfBirthFormatValidatorTest {

  @Test
  @Parameters(value = [
    "24/04/1971, VALID",
    "24-04-1971, INVALID_PATTERN",
    "1971-04-24, INVALID_PATTERN",
    "1971-24-04, INVALID_PATTERN",
    "24/04, INVALID_PATTERN",
    " , INVALID_PATTERN"
  ])
  fun `validate (v1)`(date: String, expectedResult: Result) {
    val format = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

    assertThat(DateOfBirthFormatValidator(format).validate(date)).isEqualTo(expectedResult)
  }

  @Test
  @Parameters(method = "params for v2 validation")
  fun `validate (v2)`(date: String, expectedResult: DateOfBirthFormatValidator.Result2) {
    val format = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

    assertThat(DateOfBirthFormatValidator(format).validate2(date)).isEqualTo(expectedResult)
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
  fun `validate future date (v1)`() {
    val format = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
    val dobValidator = DateOfBirthFormatValidator(format)
    val futureDateResult = dobValidator.validate("01/01/3000", nowDate = LocalDate.parse("2018-07-16"))

    assertThat(futureDateResult).isEqualTo(Result.DATE_IS_IN_FUTURE)
  }

  @Test
  fun `validate future date (v2)`() {
    val format = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
    val dobValidator = DateOfBirthFormatValidator(format)
    val futureDateResult = dobValidator.validate2("01/01/3000", nowDate = LocalDate.parse("2018-07-16"))

    assertThat(futureDateResult).isEqualTo(Invalid.DateIsInFuture)
  }
}
