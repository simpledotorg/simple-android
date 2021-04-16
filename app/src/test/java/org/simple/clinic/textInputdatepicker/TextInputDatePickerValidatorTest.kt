package org.simple.clinic.textInputdatepicker

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.NotValid.DateIsInPast
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.NotValid.InvalidPattern
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.NotValid.MaximumAllowedDateRange
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Valid
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TextInputDatePickerValidatorTest {

  private val userEnteredDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val textInputDatePickerValidator = TextInputDatePickerValidator(
      userEnteredDateFormat = userEnteredDateFormat
  )
  private val minDate = LocalDate.parse("2021-03-01")
  private val maxDate = LocalDate.parse("2022-03-01")

  @Test
  fun `it should return with an invalid pattern error if date is invalid`() {
    val result = textInputDatePickerValidator.validate(minDate, maxDate, "14/45/2021")

    assertThat(result).isEqualTo(InvalidPattern)
  }

  @Test
  fun `it should return with a maximum date range allowed error if the date is in future`() {
    val result = textInputDatePickerValidator.validate(minDate, maxDate, "14/04/2023")

    assertThat(result).isEqualTo(MaximumAllowedDateRange)
  }

  @Test
  fun `it should return with a date is in past error if the date is in past`() {
    val result = textInputDatePickerValidator.validate(minDate, maxDate, "14/04/2020")

    assertThat(result).isEqualTo(DateIsInPast)
  }

  @Test
  fun `it should return with a valid date if the date is in the range`() {
    val result = textInputDatePickerValidator.validate(minDate, maxDate, "14/04/2021")

    assertThat(result).isEqualTo(Valid(LocalDate.of(2021, 4, 14)))
  }
}
