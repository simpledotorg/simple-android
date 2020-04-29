package org.simple.clinic.registration.pin

import org.simple.clinic.widgets.UiEvent

class RegistrationPinScreenCreated : UiEvent

data class RegistrationPinTextChanged(val pin: String) : UiEvent {
  override val analyticsName = "Registration:Pin Entry:Pin Text Changed"
}

class RegistrationPinDoneClicked : UiEvent {
  override val analyticsName = "Registration:Pin Entry:Done Clicked"
}
