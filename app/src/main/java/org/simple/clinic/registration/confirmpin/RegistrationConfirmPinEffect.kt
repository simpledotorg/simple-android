package org.simple.clinic.registration.confirmpin

import org.simple.clinic.user.OngoingRegistrationEntry

sealed class RegistrationConfirmPinEffect

data class ValidatePinConfirmation(
    val pinConfirmation: String,
    val entry: OngoingRegistrationEntry
) : RegistrationConfirmPinEffect()

sealed class RegistrationConfirmPinViewEffect : RegistrationConfirmPinEffect()

object ClearPin : RegistrationConfirmPinViewEffect()

data class OpenFacilitySelectionScreen(val entry: OngoingRegistrationEntry) : RegistrationConfirmPinViewEffect()

data class GoBackToPinEntry(val entry: OngoingRegistrationEntry) : RegistrationConfirmPinViewEffect()
