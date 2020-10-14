package org.simple.clinic.security.pin

import org.simple.clinic.security.pin.BruteForceProtection.ProtectedState
import org.simple.clinic.security.pin.verification.PinVerificationMethod
import org.simple.clinic.widgets.UiEvent

sealed class PinEntryEvent : UiEvent

data class PinTextChanged(val pin: String) : PinEntryEvent() {
  override val analyticsName = "PIN text changed"
}

data class PinEntryStateChanged(val state: ProtectedState) : PinEntryEvent()

data class PinAuthenticated(val data: Any?) : PinEntryEvent() {
  override val analyticsName = "PIN authenticated"
}

data class PinVerified(val result: PinVerificationMethod.VerificationResult) : PinEntryEvent()

object PinEntryDoneClicked : PinEntryEvent()
