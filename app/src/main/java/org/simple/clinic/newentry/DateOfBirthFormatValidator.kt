package org.simple.clinic.newentry

import org.simple.clinic.patient.PatientRepository
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.format.DateTimeParseException
import javax.inject.Inject

class DateOfBirthFormatValidator @Inject constructor() {

  enum class Result {
    VALID,
    INVALID_PATTERN,
    DATE_IS_IN_FUTURE,
  }

  fun validate(dateText: String, nowDate: LocalDate = LocalDate.now(UTC)): Result {
    return try {
      if (dateText.isBlank()) {
        Result.INVALID_PATTERN
      }

      val parsedDate = PatientRepository.dateOfTimeFormatter.parse(dateText, LocalDate::from)
      when {
        parsedDate > nowDate -> Result.DATE_IS_IN_FUTURE
        else -> Result.VALID
      }

    } catch (e: Exception) {
      when (e) {
        is DateTimeParseException -> Result.INVALID_PATTERN
        else -> throw e
      }
    }
  }
}
