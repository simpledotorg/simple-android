package org.simple.clinic.registration.pin

import org.simple.clinic.widgets.UiEvent

sealed class RegistrationPinEvent : UiEvent

data class RegistrationPinTextChanged(val pin: String) : RegistrationPinEvent() {
  override val analyticsName = "Registration:Pin Entry:Pin Text Changed"
}

class RegistrationPinDoneClicked : RegistrationPinEvent() {
  override val analyticsName = "Registration:Pin Entry:Done Clicked"
}
