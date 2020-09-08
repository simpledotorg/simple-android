package org.simple.clinic.security.pin.verification

import org.simple.clinic.login.activateuser.ActivateUser
import org.simple.clinic.login.activateuser.ActivateUser.Result
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Correct
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Incorrect
import org.simple.clinic.summary.teleconsultation.api.TeleconsultPhoneNumber
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.OngoingLoginEntryRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class LoginPinServerVerificationMethod @Inject constructor(
    private val repository: OngoingLoginEntryRepository,
    private val activateUser: ActivateUser
) : PinVerificationMethod {

  override fun verify(pin: String): VerificationResult {
    val entry = repository.entryImmediate()

    return when (val result = activateUser.activate(entry.uuid, pin)) {
      is Result.Success -> Correct(UserData.create(pin, result.userPayload))
      Result.IncorrectPin -> Incorrect(pin)
      Result.NetworkError -> VerificationResult.NetworkError
      is Result.ServerError -> VerificationResult.ServerError
      is Result.OtherError -> VerificationResult.OtherError(result.cause)
    }
  }

  data class UserData(
      val pin: String,
      val uuid: UUID,
      val fullName: String,
      val phoneNumber: String,
      val pinDigest: String,
      val registrationFacilityUuid: UUID,
      val status: UserStatus,
      val createdAt: Instant,
      val updatedAt: Instant,
      val teleconsultPhoneNumber: String?,
      val capabilities: User.Capabilities?
  ) {

    companion object {
      fun create(
          pin: String,
          payload: LoggedInUserPayload
      ): UserData {
        return UserData(
            pin = pin,
            uuid = payload.uuid,
            fullName = payload.fullName,
            phoneNumber = payload.phoneNumber,
            pinDigest = payload.pinDigest,
            registrationFacilityUuid = payload.registrationFacilityId,
            status = payload.status,
            createdAt = payload.createdAt,
            updatedAt = payload.updatedAt,
            teleconsultPhoneNumber = payload.teleconsultPhoneNumber,
            capabilities = payload.capabilities
        )
      }
    }
  }
}
