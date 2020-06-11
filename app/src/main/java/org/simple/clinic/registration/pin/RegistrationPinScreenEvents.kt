package org.simple.clinic.registration.pin

import org.simple.clinic.widgets.UiEvent

class RegistrationPinScreenCreated : UiEvent

class RegistrationPinDoneClicked : UiEvent {
  override val analyticsName = "Registration:Pin Entry:Done Clicked"
}
