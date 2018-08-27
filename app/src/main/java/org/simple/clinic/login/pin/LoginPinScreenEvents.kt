package org.simple.clinic.login.pin

import org.simple.clinic.widgets.UiEvent

data class PinTextChanged(val pin: String) : UiEvent {
  override val analyticsName = "Login:Pin Entry:Pin Text Changed"
}

class PinSubmitClicked : UiEvent {
  override val analyticsName = "Login:Pin Entry:Submit Clicked"
}

class PinScreenCreated : UiEvent

class PinBackClicked : UiEvent {
  override val analyticsName = "Login:Pin Entry:Back Clicked"
}

// This event does not need an analytics name because it is
// part of the old, deprecated login flow and will be removed
data class LoginPinOtpReceived(val otp: String) : UiEvent
