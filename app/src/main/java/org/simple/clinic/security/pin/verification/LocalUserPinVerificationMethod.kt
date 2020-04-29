package org.simple.clinic.security.pin.verification

import org.simple.clinic.security.ComparisonResult.DIFFERENT
import org.simple.clinic.security.ComparisonResult.SAME
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Correct
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Incorrect
import org.simple.clinic.user.UserSession
import javax.inject.Inject

class LocalUserPinVerificationMethod @Inject constructor(
    private val userSession: UserSession,
    private val passwordHasher: PasswordHasher
) : PinVerificationMethod {

  override fun verify(pin: String): VerificationResult {
    val user = userSession.loggedInUserImmediate()
    requireNotNull(user)

    return when (passwordHasher.compare(user.pinDigest, pin)) {
      SAME -> Correct(pin)
      DIFFERENT -> Incorrect(pin)
    }
  }
}
