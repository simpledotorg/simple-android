package org.simple.clinic.registration.register

import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.User

sealed class RegistrationLoadingEffect

data class RegisterUserAtFacility(val user: User) : RegistrationLoadingEffect()

data class ConvertRegistrationEntryToUserDetails(val registrationEntry: OngoingRegistrationEntry) : RegistrationLoadingEffect()

sealed class RegistrationLoadingViewEffect : RegistrationLoadingEffect()

object GoToHomeScreen : RegistrationLoadingViewEffect()
