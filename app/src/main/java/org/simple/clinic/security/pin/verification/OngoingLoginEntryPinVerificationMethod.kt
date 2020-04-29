package org.simple.clinic.security.pin.verification

import org.simple.clinic.security.ComparisonResult.DIFFERENT
import org.simple.clinic.security.ComparisonResult.SAME
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Correct
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Incorrect
import org.simple.clinic.user.OngoingLoginEntryRepository
import javax.inject.Inject

class OngoingLoginEntryPinVerificationMethod @Inject constructor(
    private val ongoingLoginEntryRepository: OngoingLoginEntryRepository,
    private val passwordHasher: PasswordHasher
) : PinVerificationMethod {

  override fun verify(pin: String): VerificationResult {
    val entry = ongoingLoginEntryRepository.entryImmediate()

    return when (passwordHasher.compare(entry.pinDigest!!, pin)) {
      SAME -> Correct(pin)
      DIFFERENT -> Incorrect(pin)
    }
  }
}
