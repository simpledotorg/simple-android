package org.simple.clinic.security.pin

import org.simple.clinic.widgets.UiEvent

sealed class PinEntryEvent : UiEvent

data class PinTextChanged(val pin: String) : PinEntryEvent() {
  override val analyticsName = "PIN text changed"
}

data class PinDigestToVerify(val pinDigest: String) : PinEntryEvent()

object CorrectPinEntered : PinEntryEvent()

object WrongPinEntered : PinEntryEvent()
