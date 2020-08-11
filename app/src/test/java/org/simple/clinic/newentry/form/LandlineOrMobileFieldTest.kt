package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.newentry.form.ValidationError.LengthTooLong
import org.simple.clinic.newentry.form.ValidationError.LengthTooShort
import org.simple.clinic.newentry.form.ValidationError.MissingValue

@RunWith(JUnitParamsRunner::class)
class LandlineOrMobileFieldTest {
  private val landlineOrMobileField = LandlineOrMobileField(_labelResId = 0)

  @Test
  fun `it returns a missing value error when the field is empty`() {
    assertThat(landlineOrMobileField.validate(""))
        .containsExactly(MissingValue)
  }

  @Test
  fun `it returns a missing value error when the field is blank`() {
    assertThat(landlineOrMobileField.validate("    "))
        .containsExactly(MissingValue)
  }

  @Test
  @Parameters("123456", "1234567890", "123456789012")
  fun `it returns an empty set if the number is valid`(number: String) {
    assertThat(landlineOrMobileField.validate(number))
        .isEmpty()
  }

  @Test
  fun `it returns length too short if the number is less than 6 characters`() {
    assertThat(landlineOrMobileField.validate("12345"))
        .containsExactly(LengthTooShort)
  }

  @Test
  fun `it returns length too long if the number is greater than 12 characters`() {
    assertThat(landlineOrMobileField.validate("1234567890123"))
        .containsExactly(LengthTooLong)
  }
}
