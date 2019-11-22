package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.LengthTooLong
import org.simple.clinic.newentry.form.ValidationError.LengthTooShort
import org.simple.clinic.newentry.form.ValidationError.MissingValue

class LandlineOrMobileFieldTest {
  private val landlineOrMobileField = LandlineOrMobileField()

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
  fun `it returns an empty set if the number is valid`() {
    assertThat(landlineOrMobileField.validate("1234567890"))
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
