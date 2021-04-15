package org.simple.clinic.bp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BloodPressureReading(
    val systolic: Int,
    val diastolic: Int
) : Parcelable {

  fun validate(): ValidationResult {
    return when {
      systolic < 70 -> ValidationResult.ErrorSystolicTooLow
      systolic > 300 -> ValidationResult.ErrorSystolicTooHigh
      diastolic < 40 -> ValidationResult.ErrorDiastolicTooLow
      diastolic > 180 -> ValidationResult.ErrorDiastolicTooHigh
      systolic < diastolic -> ValidationResult.ErrorSystolicLessThanDiastolic
      else -> ValidationResult.Valid(this)
    }
  }
}
