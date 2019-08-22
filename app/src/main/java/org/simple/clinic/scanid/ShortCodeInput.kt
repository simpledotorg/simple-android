package org.simple.clinic.scanid

import org.simple.clinic.SHORT_CODE_REQUIRED_LENGTH
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.NotEqualToRequiredLength
import org.simple.clinic.scanid.ShortCodeValidationResult.Success

data class ShortCodeInput(val shortCodeText: String) {
  fun validate(): ShortCodeValidationResult {
    return when {
      shortCodeText.isEmpty() -> Empty
      shortCodeText.length !=  SHORT_CODE_REQUIRED_LENGTH -> NotEqualToRequiredLength
      shortCodeText.length == SHORT_CODE_REQUIRED_LENGTH -> Success
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
