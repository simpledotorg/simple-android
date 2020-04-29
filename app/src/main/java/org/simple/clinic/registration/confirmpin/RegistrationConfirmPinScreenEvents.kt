package org.simple.clinic.registration.confirmpin

import org.simple.clinic.widgets.UiEvent

class RegistrationConfirmPinScreenCreated : UiEvent

data class RegistrationConfirmPinTextChanged(val confirmPin: String) : UiEvent {
  override val analyticsName = "Registration:Confirm Pin:Pin Text Changed"
}

class RegistrationConfirmPinDoneClicked : UiEvent {
  override val analyticsName = "Registration:Confirm Pin:Done Clicked"
}

class RegistrationResetPinClicked : UiEvent {
  override val analyticsName = "Registration:Confirm PIN:Reset Clicked"
}

class RegistrationConfirmPinValidated(val enteredPin: String, val valid: Boolean) : UiEvent
