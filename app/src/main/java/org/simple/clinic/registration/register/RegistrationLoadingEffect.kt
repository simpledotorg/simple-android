package org.simple.clinic.registration.register

import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User

sealed class RegistrationLoadingEffect

object LoadRegistrationDetails: RegistrationLoadingEffect()

data class RegisterUserAtFacility(val user: User, val facility: Facility): RegistrationLoadingEffect()

object ClearCurrentRegistrationEntry: RegistrationLoadingEffect()
