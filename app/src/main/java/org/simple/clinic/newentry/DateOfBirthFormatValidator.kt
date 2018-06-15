package org.simple.clinic.newentry

import org.simple.clinic.patient.PatientRepository
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeParseException
import javax.inject.Inject

class DateOfBirthFormatValidator @Inject constructor() {

  enum class Result {
    VALID,
    INVALID
  }

  fun validate(dateText: String): Result {
    return try {
      PatientRepository.dateOfTimeFormatter.parse(dateText, LocalDate::from)
      Result.VALID

    } catch (e: Exception) {
      when (e) {
        is NullPointerException -> Result.INVALID
        is DateTimeParseException -> Result.INVALID
        else -> throw e
      }
    }
  }
}
