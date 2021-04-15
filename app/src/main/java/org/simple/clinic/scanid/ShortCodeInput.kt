package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport.SHORT_CODE_LENGTH
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.NotEqualToRequiredLength
import org.simple.clinic.scanid.ShortCodeValidationResult.Success

@Parcelize
data class ShortCodeInput(val shortCodeText: String) : Parcelable {
  fun validate(): ShortCodeValidationResult {
    return when {
      shortCodeText.isEmpty() -> Empty
      shortCodeText.length != SHORT_CODE_LENGTH -> NotEqualToRequiredLength
      shortCodeText.length == SHORT_CODE_LENGTH -> Success
      else -> throw UnsupportedOperationException("Unknown situation for short code input: $shortCodeText")
    }
  }
}

sealed class ShortCodeValidationResult {
  object Success : ShortCodeValidationResult()

  sealed class Failure : ShortCodeValidationResult() {
    object Empty : Failure()
    object NotEqualToRequiredLength : Failure()
  }
}
