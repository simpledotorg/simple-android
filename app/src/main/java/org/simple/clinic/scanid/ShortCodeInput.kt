package org.simple.clinic.scanid

import org.simple.clinic.SHORT_CODE_REQUIRED_LENGTH
import org.simple.clinic.scanid.ShortCodeValidationResult.Empty

data class ShortCodeInput(val shortCodeText: String) {
  fun validate(): ShortCodeValidationResult {
    return when {
      shortCodeText.isEmpty() -> Empty
      shortCodeText.length !=  SHORT_CODE_REQUIRED_LENGTH -> ShortCodeValidationResult.NotEqualToRequiredLength
      shortCodeText.length == SHORT_CODE_REQUIRED_LENGTH -> ShortCodeValidationResult.Success
      else -> throw UnsupportedOperationException("Unknown situation for short code input: $shortCodeText")
    }
  }
}

sealed class ShortCodeValidationResult {
  object Success : ShortCodeValidationResult()

  object Empty : ShortCodeValidationResult()
  object NotEqualToRequiredLength : ShortCodeValidationResult()
}
