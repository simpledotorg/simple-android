package org.simple.clinic.registration.confirmpin

import org.simple.clinic.user.OngoingRegistrationEntry

sealed class RegistrationConfirmPinEffect

data class ValidatePinConfirmation(
    val pinConfirmation: String,
    val entry: OngoingRegistrationEntry
): RegistrationConfirmPinEffect()

object ClearPin: RegistrationConfirmPinEffect()
