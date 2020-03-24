package org.simple.clinic.security.pin

sealed class PinEntryEffect

data class ValidateEnteredPin(val enteredPin: String, val pinDigest: String): PinEntryEffect()

object LoadPinEntryProtectedStates: PinEntryEffect()

object HideError: PinEntryEffect()

data class ShowIncorrectPinError(val attemptsMade: Int, val attemptsRemaining: Int): PinEntryEffect()

data class ShowIncorrectPinLimitReachedError(val attemptsMade: Int): PinEntryEffect()
