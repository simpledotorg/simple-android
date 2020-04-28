package org.simple.clinic.bp.entry

import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooLow
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicLessThanDiastolic
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooLow
import org.simple.clinic.bp.entry.BpValidator.Validation.Success
import javax.inject.Inject

class BpValidator @Inject constructor() {

  // TODO: Rename to "Result".
  sealed class Validation {
    // TODO: Rename to "Valid".
    data class Success(
        val systolic: Int,
        val diastolic: Int
    ) : Validation()

    object ErrorSystolicEmpty : Validation()
    object ErrorDiastolicEmpty : Validation()
    object ErrorSystolicTooHigh : Validation()
    object ErrorSystolicTooLow : Validation()
    object ErrorDiastolicTooHigh : Validation()
    object ErrorDiastolicTooLow : Validation()
    object ErrorSystolicLessThanDiastolic : Validation()
  }

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
