package org.simple.clinic.registration.name

import org.simple.clinic.user.OngoingRegistrationEntry

sealed class RegistrationNameEffect

data class PrefillFields(val entry: OngoingRegistrationEntry) : RegistrationNameEffect()

data class ValidateEnteredName(val name: String) : RegistrationNameEffect()

data class ProceedToPinEntry(val entry: OngoingRegistrationEntry) : RegistrationNameEffect()
