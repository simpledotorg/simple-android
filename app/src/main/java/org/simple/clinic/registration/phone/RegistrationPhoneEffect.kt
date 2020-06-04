package org.simple.clinic.registration.phone

import org.simple.clinic.user.OngoingRegistrationEntry

sealed class RegistrationPhoneEffect

data class PrefillFields(val entry: OngoingRegistrationEntry): RegistrationPhoneEffect()

object LoadCurrentRegistrationEntry: RegistrationPhoneEffect()
