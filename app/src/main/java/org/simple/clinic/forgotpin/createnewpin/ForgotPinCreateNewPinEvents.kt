package org.simple.clinic.forgotpin.createnewpin

import org.simple.clinic.widgets.UiEvent

data class ForgotPinCreateNewPinTextChanged(val pin: String) : UiEvent {
  override val analyticsName = "Forgot PIN:Create new PIN:Text Changed"
}

object ForgotPinCreateNewPinSubmitClicked : UiEvent {
  override val analyticsName = "Forgot PIN:Create new PIN:Submit Clicked"
}

object ForgotPinCreateNewPinFacilityClicked : UiEvent {
  override val analyticsName = "Forgot PIN:Create new PIN:Facility Clicked"
}
