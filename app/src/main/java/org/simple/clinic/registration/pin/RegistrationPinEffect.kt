package org.simple.clinic.registration.pin

import org.simple.clinic.user.OngoingRegistrationEntry

sealed class RegistrationPinEffect

data class SaveCurrentOngoingEntry(val entry: OngoingRegistrationEntry): RegistrationPinEffect()
