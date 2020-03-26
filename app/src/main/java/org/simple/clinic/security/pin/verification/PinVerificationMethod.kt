package org.simple.clinic.security.pin.verification

interface PinVerificationMethod {

  fun verify(pin: String): VerificationResult

  sealed class VerificationResult {

    data class Correct(val data: Any? = null) : VerificationResult()

    data class Incorrect(val data: Any? = null) : VerificationResult()

    data class Failure(val error: Throwable) : VerificationResult()
  }
}
