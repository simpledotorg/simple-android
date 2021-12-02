package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.DateIsInFuture
import org.simple.clinic.newentry.form.ValidationError.InvalidDateFormat
import org.simple.clinic.newentry.form.ValidationError.MissingValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateOfBirthFieldTest {
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  private val dateOfBirthField = DateOfBirthField(
      { value -> LocalDate.parse(value, dateTimeFormatter) },
      LocalDate.parse("20/11/2019", dateTimeFormatter),
      _labelResId = 0
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

  @Test
  fun `it returns an empty list when date of birth is less than the current date`() {
    assertThat(dateOfBirthField.validate("19/11/2019"))
        .isEmpty()
  }

  @Test
  fun `it returns an empty list when date of birth is same as the current date`() {
    assertThat(dateOfBirthField.validate("20/11/2019"))
        .isEmpty()
  }
}
