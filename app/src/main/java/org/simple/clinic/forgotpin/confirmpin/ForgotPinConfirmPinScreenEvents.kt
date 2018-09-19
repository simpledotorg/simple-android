package org.simple.clinic.forgotpin.confirmpin

import org.simple.clinic.widgets.UiEvent

object ForgotPinConfirmPinScreenFacilityClicked : UiEvent {
  override val analyticsName = "Forgot PIN:Confirm PIN:Facility Clicked"
}

object ForgotPinConfirmPinScreenBackClicked : UiEvent {
  override val analyticsName = "Forgot PIN:Confirm PIN:Back Clicked"
}

data class ForgotPinConfirmPinScreenCreated(val pin: String) : UiEvent {
  init {
    if (pin.isBlank()) throw AssertionError("PIN cannot be blank!")
  }
}
