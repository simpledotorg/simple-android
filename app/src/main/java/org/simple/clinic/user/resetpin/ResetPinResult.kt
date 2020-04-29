package org.simple.clinic.user.resetpin

sealed class ResetPinResult {
  object Success : ResetPinResult()
  object NetworkError : ResetPinResult()
  object UserNotFound : ResetPinResult()
  data class UnexpectedError(val cause: Throwable? = null) : ResetPinResult()
}
