package org.simple.clinic.user.resetpin

sealed class ResetPinResult {
  data object Success : ResetPinResult()
  data object NetworkError : ResetPinResult()
  data object UserNotFound : ResetPinResult()
  data class UnexpectedError(val cause: Throwable? = null) : ResetPinResult()
}
