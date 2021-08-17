package org.simple.clinic.registration.phone

import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserStatus
import java.util.UUID

sealed class RegistrationPhoneEffect

data class ValidateEnteredNumber(val number: String) : RegistrationPhoneEffect()

object SyncFacilities : RegistrationPhoneEffect()

data class SearchForExistingUser(val number: String) : RegistrationPhoneEffect()

data class CreateUserLocally(
    val userUuid: UUID,
    val number: String,
    val status: UserStatus
) : RegistrationPhoneEffect()

object LoadCurrentUserUnauthorizedStatus : RegistrationPhoneEffect()

sealed class RegistrationPhoneViewEffect : RegistrationPhoneEffect()

data class PrefillFields(val entry: OngoingRegistrationEntry) : RegistrationPhoneViewEffect()

data class ShowAccessDeniedScreen(val number: String) : RegistrationPhoneViewEffect()

object ProceedToLogin : RegistrationPhoneViewEffect()

object ShowUserLoggedOutAlert : RegistrationPhoneViewEffect()

data class ContinueRegistration(val entry: OngoingRegistrationEntry) : RegistrationPhoneViewEffect()
