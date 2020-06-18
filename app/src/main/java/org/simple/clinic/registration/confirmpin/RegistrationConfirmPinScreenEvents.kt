package org.simple.clinic.registration.confirmpin

import org.simple.clinic.widgets.UiEvent

class RegistrationConfirmPinScreenCreated : UiEvent

class RegistrationResetPinClicked : UiEvent {
  override val analyticsName = "Registration:Confirm PIN:Reset Clicked"
}
