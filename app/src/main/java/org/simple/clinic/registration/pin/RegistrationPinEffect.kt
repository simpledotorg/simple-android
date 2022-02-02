package org.simple.clinic.registration.pin

import org.simple.clinic.user.OngoingRegistrationEntry

sealed class RegistrationPinEffect

sealed class RegistrationPinViewEffect : RegistrationPinEffect()

data class ProceedToConfirmPin(val entry: OngoingRegistrationEntry) : RegistrationPinViewEffect()
