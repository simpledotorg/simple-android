package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result
import org.threeten.bp.LocalDate

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
  fun validate(date: String, expectedResult: Result) {
    assertThat(DateOfBirthFormatValidator().validate(date)).isEqualTo(expectedResult)
  }

  @Test
  fun `validate future date`() {
    val dobValidator = DateOfBirthFormatValidator()
    val futureDateResult = dobValidator.validate("01/01/3000", nowDate = LocalDate.parse("2018-07-16"))
    assertThat(futureDateResult).isEqualTo(Result.DATE_IS_IN_FUTURE)
  }
}
