package org.simple.clinic.pin

import org.simple.clinic.widgets.UiEvent

data class PinTextChanged(val pin: String) : UiEvent {
  override val analyticsName = "PIN text changed"
}

data class PinSubmitClicked(val pin: String) : UiEvent {
  override val analyticsName = "PIN submitted"
}

class PinAuthenticated : UiEvent {
  override val analyticsName = "PIN authenticated"
}
