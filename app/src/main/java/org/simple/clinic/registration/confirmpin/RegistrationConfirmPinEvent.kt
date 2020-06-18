package org.simple.clinic.registration.confirmpin

import org.simple.clinic.widgets.UiEvent

sealed class RegistrationConfirmPinEvent : UiEvent

data class RegistrationConfirmPinTextChanged(val confirmPin: String) : RegistrationConfirmPinEvent() {
  override val analyticsName = "Registration:Confirm Pin:Pin Text Changed"
}

data class PinConfirmationValidated(val result: RegistrationConfirmPinValidationResult) : RegistrationConfirmPinEvent()

class RegistrationConfirmPinDoneClicked : RegistrationConfirmPinEvent() {
  override val analyticsName = "Registration:Confirm Pin:Done Clicked"
}

object RegistrationEntrySaved: RegistrationConfirmPinEvent()
