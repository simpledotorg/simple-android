package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport.SHORT_CODE_LENGTH
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure.NotEqualToRequiredLength
import org.simple.clinic.scanid.EnteredCodeValidationResult.Success

@Parcelize
data class EnteredCodeInput(val enteredCodeText: String) : Parcelable {

  @IgnoredOnParcel
  private val longCodeLength = 14

  fun validate(): EnteredCodeValidationResult {
    return when {
      enteredCodeText.isEmpty() -> Empty
      enteredCodeText.length != SHORT_CODE_LENGTH && enteredCodeText.length != longCodeLength -> NotEqualToRequiredLength
      enteredCodeText.length == SHORT_CODE_LENGTH || enteredCodeText.length == longCodeLength -> Success
      else -> throw UnsupportedOperationException("Unknown situation for entered code input: $enteredCodeText")
    }
  }
}

sealed class EnteredCodeValidationResult {
  object Success : EnteredCodeValidationResult()

  sealed class Failure : EnteredCodeValidationResult() {
    object Empty : Failure()
    object NotEqualToRequiredLength : Failure()
  }
}
