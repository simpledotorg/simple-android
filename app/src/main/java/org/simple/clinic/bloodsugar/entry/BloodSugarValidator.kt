package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarEmpty
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooHigh
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooLow
import javax.inject.Inject

class BloodSugarValidator @Inject constructor() {

  sealed class Result {
    data class Valid(val bloodSugarReading: Int) : Result()

    object ErrorBloodSugarEmpty : Result()
    object ErrorBloodSugarTooHigh : Result()
    object ErrorBloodSugarTooLow : Result()
  }

  fun validate(bloodSugarReading: String): Result {
    if (bloodSugarReading.isBlank()) {
      return ErrorBloodSugarEmpty
    }

    val bloodSugarNumber = bloodSugarReading.trim().toInt()

    return when {
      bloodSugarNumber < 30 -> ErrorBloodSugarTooLow
      bloodSugarNumber > 1000 -> ErrorBloodSugarTooHigh
      else -> Result.Valid(bloodSugarNumber)
    }
  }
}
