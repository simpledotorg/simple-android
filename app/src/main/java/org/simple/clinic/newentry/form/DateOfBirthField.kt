package org.simple.clinic.newentry.form

import org.simple.clinic.newentry.form.ValidationError.DateIsInFuture
import org.simple.clinic.newentry.form.ValidationError.InvalidDateFormat
import org.simple.clinic.newentry.form.ValidationError.MissingValue
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

class DateOfBirthField(
    private val dateTimeFormatter: DateTimeFormatter,
    private val today: LocalDate
) : InputField<String>() {
  override fun validate(value: String): List<ValidationError> {
    if (value.isBlank()) return listOf(MissingValue)

    return try {
      val parsedDate = LocalDate.parse(value, dateTimeFormatter)
      if (parsedDate > today) {
        listOf(DateIsInFuture)
      } else {
        emptyList()
      }
    } catch (e: DateTimeParseException) {
      listOf(InvalidDateFormat)
    }
  }
}
