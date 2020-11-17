package org.simple.clinic.registration.register

import org.simple.clinic.user.User

sealed class RegistrationLoadingEffect

object LoadRegistrationDetails: RegistrationLoadingEffect()

data class RegisterUserAtFacility(val user: User): RegistrationLoadingEffect()

object GoToHomeScreen: RegistrationLoadingEffect()
