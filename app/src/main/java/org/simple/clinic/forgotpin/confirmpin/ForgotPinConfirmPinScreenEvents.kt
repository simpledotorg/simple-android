package org.simple.clinic.forgotpin.confirmpin

import org.simple.clinic.widgets.UiEvent

data class ForgotPinConfirmPinScreenCreated(val pin: String) : UiEvent {
  init {
    if (pin.isBlank()) throw AssertionError("PIN cannot be blank!")
  }
}

data class ForgotPinConfirmPinSubmitClicked(val pin: String) : UiEvent {
  override val analyticsName = "Forgot PIN:Confirm PIN:Submit Clicked"
}

data class ForgotPinConfirmPinTextChanged(val text: String) : UiEvent {
  override val analyticsName = "Forgot PIN:Confirm PIN:Text Changed"
}
