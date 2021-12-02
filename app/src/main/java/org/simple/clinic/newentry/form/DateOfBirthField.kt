package org.simple.clinic.newentry.form

import kotlinx.parcelize.Parcelize
import org.simple.clinic.newentry.form.ValidationError.DateIsInFuture
import org.simple.clinic.newentry.form.ValidationError.InvalidDateFormat
import org.simple.clinic.newentry.form.ValidationError.MissingValue
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Parcelize
data class DateOfBirthField(
    private val parseDate: (String) -> LocalDate,
    private val today: LocalDate,
    private val _labelResId: Int
) : InputField<String>(_labelResId) {
  override fun validate(value: String): Set<ValidationError> {
    if (value.isBlank()) return setOf(MissingValue)

    return try {
      if (parseDate(value) > today) {
        setOf(DateIsInFuture)
      } else {
        emptySet()
      }
    } catch (e: DateTimeParseException) {
      setOf(InvalidDateFormat)
    }
  }
}
