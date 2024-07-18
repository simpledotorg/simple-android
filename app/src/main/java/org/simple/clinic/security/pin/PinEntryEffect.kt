package org.simple.clinic.security.pin

import java.time.Instant

sealed class PinEntryEffect

data object LoadPinEntryProtectedStates : PinEntryEffect()

data object HideError : PinEntryEffect()

data class ShowIncorrectPinError(
    val attemptsMade: Int,
    val attemptsRemaining: Int
) : PinEntryEffect()

data class ShowIncorrectPinLimitReachedError(val attemptsMade: Int) : PinEntryEffect()

data object AllowPinEntry : PinEntryEffect()

data class BlockPinEntryUntil(val blockTill: Instant) : PinEntryEffect()

data object RecordSuccessfulAttempt : PinEntryEffect()

data object RecordFailedAttempt : PinEntryEffect()

data object ShowProgress : PinEntryEffect()

data object ClearPin : PinEntryEffect()

data class VerifyPin(val pin: String) : PinEntryEffect()

data class CorrectPinEntered(val pinVerifiedData: Any?) : PinEntryEffect()

data object ShowNetworkError : PinEntryEffect()

data object ShowServerError : PinEntryEffect()

data object ShowUnexpectedError : PinEntryEffect()

data class SaveDemoFacility(val data: Any?) : PinEntryEffect()
