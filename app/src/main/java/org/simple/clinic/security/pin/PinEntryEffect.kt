package org.simple.clinic.security.pin

import java.time.Instant

sealed class PinEntryEffect

object LoadPinEntryProtectedStates : PinEntryEffect()

object HideError : PinEntryEffect()

data class ShowIncorrectPinError(
    val attemptsMade: Int,
    val attemptsRemaining: Int
) : PinEntryEffect()

data class ShowIncorrectPinLimitReachedError(val attemptsMade: Int) : PinEntryEffect()

object AllowPinEntry : PinEntryEffect()

data class BlockPinEntryUntil(val blockTill: Instant) : PinEntryEffect()

object RecordSuccessfulAttempt : PinEntryEffect()

object RecordFailedAttempt : PinEntryEffect()

object ShowProgress : PinEntryEffect()

object ClearPin : PinEntryEffect()

data class VerifyPin(val pin: String) : PinEntryEffect()

data class CorrectPinEntered(val pinVerifiedData: Any?) : PinEntryEffect()

object ShowNetworkError : PinEntryEffect()

object ShowServerError : PinEntryEffect()

object ShowUnexpectedError : PinEntryEffect()
