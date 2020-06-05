package org.simple.clinic.registration.phone

import org.simple.clinic.user.OngoingRegistrationEntry

sealed class RegistrationPhoneEffect

data class PrefillFields(val entry: OngoingRegistrationEntry) : RegistrationPhoneEffect()

object LoadCurrentRegistrationEntry : RegistrationPhoneEffect()

object CreateNewRegistrationEntry : RegistrationPhoneEffect()

data class ValidateEnteredNumber(val number: String) : RegistrationPhoneEffect()

object SyncFacilities : RegistrationPhoneEffect()

data class SearchForExistingUser(val number: String) : RegistrationPhoneEffect()

data class ShowAccessDeniedScreen(val number: String): RegistrationPhoneEffect()
