package org.simple.clinic.bp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BloodPressureReading(
    val systolic: Int,
    val diastolic: Int
) : Parcelable {

  fun validate(): Validation {
    return when {
      systolic < 70 -> Validation.ErrorSystolicTooLow
      systolic > 300 -> Validation.ErrorSystolicTooHigh
      diastolic < 40 -> Validation.ErrorDiastolicTooLow
      diastolic > 180 -> Validation.ErrorDiastolicTooHigh
      systolic < diastolic -> Validation.ErrorSystolicLessThanDiastolic
      else -> Validation.Success(this)
    }
  }
}
