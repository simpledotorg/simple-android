package org.simple.clinic.security.pin.verification

interface PinVerificationMethod {

  fun verify(pin: String): VerificationResult

  sealed class VerificationResult {

    data class Correct(val data: Any? = null) : VerificationResult()

    data class Incorrect(val data: Any? = null) : VerificationResult()

    data object NetworkError : VerificationResult()

    data object ServerError : VerificationResult()

    data class OtherError(val cause: Throwable) : VerificationResult()
  }
}
