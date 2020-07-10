package org.simple.clinic.newentry.form

import org.simple.clinic.newentry.form.ValidationError.DateIsInFuture
import org.simple.clinic.newentry.form.ValidationError.InvalidDateFormat
import org.simple.clinic.newentry.form.ValidationError.MissingValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DateOfBirthField(
    private val dateTimeFormatter: DateTimeFormatter,
    private val today: LocalDate,
    labelResId: Int
) : InputField<String>(labelResId) {
  override fun validate(value: String): Set<ValidationError> {
    if (value.isBlank()) return setOf(MissingValue)

    return try {
      val parsedDate = LocalDate.parse(value, dateTimeFormatter)
      if (parsedDate > today) {
        setOf(DateIsInFuture)
      } else {
        emptySet()
      }
    } catch (e: DateTimeParseException) {
      setOf(InvalidDateFormat)
    }
  }
}
