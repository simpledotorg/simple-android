package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport.SHORT_CODE_LENGTH
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.NotEqualToRequiredLength
import org.simple.clinic.scanid.ShortCodeValidationResult.Success

@Parcelize
data class EnteredCodeInput(val shortCodeText: String) : Parcelable {

  @IgnoredOnParcel
  private val longCodeLength = 14

  fun validate(): ShortCodeValidationResult {
    return when {
      shortCodeText.isEmpty() -> Empty
      shortCodeText.length != SHORT_CODE_LENGTH && shortCodeText.length != longCodeLength -> NotEqualToRequiredLength
      shortCodeText.length == SHORT_CODE_LENGTH || shortCodeText.length == longCodeLength -> Success
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
