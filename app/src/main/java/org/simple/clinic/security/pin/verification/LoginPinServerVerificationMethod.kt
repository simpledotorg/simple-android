package org.simple.clinic.security.pin.verification

import org.simple.clinic.login.activateuser.ActivateUser
import org.simple.clinic.login.activateuser.ActivateUser.Result
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Correct
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Incorrect
import org.simple.clinic.user.OngoingLoginEntryRepository
import javax.inject.Inject

class LoginPinServerVerificationMethod @Inject constructor(
    private val repository: OngoingLoginEntryRepository,
    private val activateUser: ActivateUser
) : PinVerificationMethod {

  override fun verify(pin: String): VerificationResult {
    val entry = repository.entryImmediate()

    return when (val result = activateUser.activate(entry.uuid, pin)) {
      is Result.Success -> Correct(result.userPayload)
      Result.IncorrectPin -> Incorrect(pin)
      Result.NetworkError -> VerificationResult.NetworkError
      is Result.ServerError -> VerificationResult.ServerError
      is Result.OtherError -> VerificationResult.OtherError(result.cause)
    }
  }
}
