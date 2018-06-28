package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result

class DateOfBirthFormatValidatorTest {

  @Test
  fun validate() {
    assertThat(DateOfBirthFormatValidator.validate("24/04/1971")).isEqualTo(Result.VALID)
    assertThat(DateOfBirthFormatValidator.validate("24-04-1971")).isEqualTo(Result.INVALID)
    assertThat(DateOfBirthFormatValidator.validate("1971-04-24")).isEqualTo(Result.INVALID)
    assertThat(DateOfBirthFormatValidator.validate("1971-24-04")).isEqualTo(Result.INVALID)
    assertThat(DateOfBirthFormatValidator.validate("24/04")).isEqualTo(Result.INVALID)
  }
}
