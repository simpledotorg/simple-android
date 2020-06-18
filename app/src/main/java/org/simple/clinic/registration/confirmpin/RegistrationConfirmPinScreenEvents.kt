package org.simple.clinic.registration.confirmpin

import org.simple.clinic.widgets.UiEvent

class RegistrationConfirmPinScreenCreated : UiEvent

class RegistrationConfirmPinDoneClicked : UiEvent {
  override val analyticsName = "Registration:Confirm Pin:Done Clicked"
}

class RegistrationResetPinClicked : UiEvent {
  override val analyticsName = "Registration:Confirm PIN:Reset Clicked"
}

class RegistrationConfirmPinValidated(val enteredPin: String, val valid: Boolean) : UiEvent
