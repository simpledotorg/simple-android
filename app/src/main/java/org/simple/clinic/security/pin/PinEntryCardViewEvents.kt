package org.simple.clinic.security.pin

import org.simple.clinic.widgets.UiEvent

object PinEntryViewCreated : UiEvent

data class PinTextChanged(val pin: String) : UiEvent {
  override val analyticsName = "PIN text changed"
}

data class PinSubmitClicked(val pin: String) : UiEvent {
  override val analyticsName = "PIN submitted"
}

data class PinAuthenticated(val pin: String) : UiEvent {
  override val analyticsName = "PIN authenticated"
}

data class PinDigestToVerify(val pinDigest: String): UiEvent
