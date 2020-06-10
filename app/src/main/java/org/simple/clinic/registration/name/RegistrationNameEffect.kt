package org.simple.clinic.registration.name

import org.simple.clinic.user.OngoingRegistrationEntry

sealed class RegistrationNameEffect

data class PrefillFields(val entry: OngoingRegistrationEntry): RegistrationNameEffect()
