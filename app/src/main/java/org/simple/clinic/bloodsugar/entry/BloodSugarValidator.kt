package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarEmpty
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooHigh
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooLow
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.Valid
import javax.inject.Inject

class BloodSugarValidator @Inject constructor() {

  sealed class Result {
    data class Valid(val bloodSugarReading: Float) : Result()

    object ErrorBloodSugarEmpty : Result()
    data class ErrorBloodSugarTooHigh(val measurementType: BloodSugarMeasurementType) : Result()
    data class ErrorBloodSugarTooLow(val measurementType: BloodSugarMeasurementType) : Result()
  }

  fun validate(bloodSugarReading: String, measurementType: BloodSugarMeasurementType): Result {
    if (bloodSugarReading.isBlank()) {
      return ErrorBloodSugarEmpty
    }

    val bloodSugarNumber = bloodSugarReading.trim().toFloat()
    val minAllowedBloodSugarValue = if (measurementType is HbA1c) 3 else 30
    val maxAllowedBloodSugarValue = if (measurementType is HbA1c) 25 else 1000

    return when {
      bloodSugarNumber < minAllowedBloodSugarValue -> ErrorBloodSugarTooLow(measurementType)
      bloodSugarNumber > maxAllowedBloodSugarValue -> ErrorBloodSugarTooHigh(measurementType)
      else -> Valid(bloodSugarNumber)
    }
  }
}
