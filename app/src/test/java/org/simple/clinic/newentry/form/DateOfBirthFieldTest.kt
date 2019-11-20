package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.InvalidDateFormat
import org.simple.clinic.newentry.form.ValidationError.MissingValue

class DateOfBirthFieldTest {
  private val dateOfBirthField = DateOfBirthField()

  @Test
  fun `it returns missing value error when date of birth is empty`() {
    assertThat(dateOfBirthField.validate(""))
        .containsExactly(MissingValue)
  }

  @Test
  fun `it returns an invalid date format error when date of birth is invalid`() {
    assertThat(dateOfBirthField.validate("7/6"))
        .containsExactly(InvalidDateFormat)
  }
}
