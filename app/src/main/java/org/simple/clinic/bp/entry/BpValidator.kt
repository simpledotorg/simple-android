package org.simple.clinic.bp.entry

import org.simple.clinic.bp.Validation
import org.simple.clinic.bp.Validation.ErrorDiastolicTooHigh
import org.simple.clinic.bp.Validation.ErrorDiastolicTooLow
import org.simple.clinic.bp.Validation.ErrorSystolicLessThanDiastolic
import org.simple.clinic.bp.Validation.ErrorSystolicTooHigh
import org.simple.clinic.bp.Validation.ErrorSystolicTooLow
import org.simple.clinic.bp.Validation.Success
import javax.inject.Inject

class BpValidator @Inject constructor() {

  fun validate(systolic: String, diastolic: String): Validation {
    val systolicNumber = systolic.trim().toInt()
    val diastolicNumber = diastolic.trim().toInt()

    return when {
      systolicNumber < 70 -> ErrorSystolicTooLow
      systolicNumber > 300 -> ErrorSystolicTooHigh
      diastolicNumber < 40 -> ErrorDiastolicTooLow
      diastolicNumber > 180 -> ErrorDiastolicTooHigh
      systolicNumber < diastolicNumber -> ErrorSystolicLessThanDiastolic
      else -> Success(systolicNumber, diastolicNumber)
    }
  }
}
