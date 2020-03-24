package org.simple.clinic.security.pin

import org.threeten.bp.Instant

sealed class PinEntryEffect

data class ValidateEnteredPin(val enteredPin: String, val pinDigest: String): PinEntryEffect()

object LoadPinEntryProtectedStates: PinEntryEffect()

object HideError: PinEntryEffect()

data class ShowIncorrectPinError(val attemptsMade: Int, val attemptsRemaining: Int): PinEntryEffect()

data class ShowIncorrectPinLimitReachedError(val attemptsMade: Int): PinEntryEffect()

object AllowPinEntry: PinEntryEffect()

data class BlockPinEntryUntil(val blockTill: Instant): PinEntryEffect()

object RecordSuccessfulAttempt : PinEntryEffect()

object RecordFailedAttempt: PinEntryEffect()

object ShowProgress: PinEntryEffect()

object ClearPin: PinEntryEffect()
