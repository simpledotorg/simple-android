package org.simple.clinic.user

sealed class ForgotPinResult {
  object Success : ForgotPinResult()
  object NetworkError : ForgotPinResult()
  object UserNotFound : ForgotPinResult()
  data class UnexpectedError(val cause: Throwable? = null) : ForgotPinResult()
}
