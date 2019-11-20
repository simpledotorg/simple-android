package org.simple.clinic.newentry.form

import org.simple.clinic.newentry.form.ValidationError.InvalidDateFormat
import org.simple.clinic.newentry.form.ValidationError.MissingValue
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeParseException

class DateOfBirthField : InputField<String>() {
  override fun validate(value: String): List<ValidationError> {
    if (value.isBlank()) return listOf(MissingValue)

    try {
      LocalDate.parse(value)
    } catch (e: DateTimeParseException) {
      return listOf(InvalidDateFormat)
    }

    TODO()
  }
}
