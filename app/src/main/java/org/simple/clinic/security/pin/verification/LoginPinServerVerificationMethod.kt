package org.simple.clinic.security.pin.verification

import org.simple.clinic.DEMO_FACILITY_ID
import org.simple.clinic.DEMO_USER_ID
import org.simple.clinic.DEMO_USER_PHONE_NUMBER
import org.simple.clinic.DEMO_USER_PIN
import org.simple.clinic.login.activateuser.ActivateUser
import org.simple.clinic.login.activateuser.ActivateUser.Result
import org.simple.clinic.security.PasswordHasher
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Correct
import org.simple.clinic.security.pin.verification.PinVerificationMethod.VerificationResult.Incorrect
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.OngoingLoginEntryRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class LoginPinServerVerificationMethod @Inject constructor(
    private val repository: OngoingLoginEntryRepository,
    private val activateUser: ActivateUser,
    private val passwordHasher: PasswordHasher
) : PinVerificationMethod {

  override fun verify(pin: String): VerificationResult {
    val entry = repository.entryImmediate()

    if (entry.phoneNumber == DEMO_USER_PHONE_NUMBER && pin == DEMO_USER_PIN) {
      /**
       * IMPORTANT: We are using this just for Google Play review team,
       * this ensures we are allowing them to test locally without linking
       * to server which has actual PHI.
       */
      return Correct(demoUserData(pin))
    }

    return when (val result = activateUser.activate(entry.uuid, pin)) {
      is Result.Success -> Correct(UserData.create(pin, result.userPayload))
      Result.IncorrectPin -> Incorrect(pin)
      Result.NetworkError -> VerificationResult.NetworkError
      is Result.ServerError -> VerificationResult.ServerError
      is Result.OtherError -> VerificationResult.OtherError(result.cause)
    }
  }

  private fun demoUserData(pin: String) = UserData.create(
      pin = pin,
      payload = LoggedInUserPayload(
          uuid = DEMO_USER_ID,
          fullName = "Demo User",
          phoneNumber = DEMO_USER_PHONE_NUMBER,
          pinDigest = passwordHasher.hash(DEMO_USER_PIN),
          registrationFacilityId = DEMO_FACILITY_ID,
          status = UserStatus.WaitingForApproval,
          createdAt = Instant.parse("2018-01-01T00:00:00Z"),
          updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
          teleconsultPhoneNumber = null,
          capabilities = User.Capabilities(canTeleconsult = User.CapabilityStatus.No)
      )
  )

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
