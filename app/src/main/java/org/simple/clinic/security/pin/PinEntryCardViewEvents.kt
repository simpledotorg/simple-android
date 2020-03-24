package org.simple.clinic.security.pin

import org.simple.clinic.widgets.UiEvent

object PinEntryViewCreated : UiEvent

data class PinSubmitClicked(val pin: String) : UiEvent {
  override val analyticsName = "PIN submitted"
}
