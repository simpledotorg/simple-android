package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.DateIsInFuture
import org.simple.clinic.newentry.form.ValidationError.InvalidDateFormat
import org.simple.clinic.newentry.form.ValidationError.MissingValue
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class DateOfBirthFieldTest {
  private val dateOfBirthField = DateOfBirthField(
      DateTimeFormatter.ofPattern("dd/MM/yyyy"),
      LocalDate.of(2019, 11, 20)
  )

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

  @Test
  fun `it returns date is in future when date of birth is in the future`() {
    assertThat(dateOfBirthField.validate("21/11/2019"))
        .containsExactly(DateIsInFuture)
  }
}
