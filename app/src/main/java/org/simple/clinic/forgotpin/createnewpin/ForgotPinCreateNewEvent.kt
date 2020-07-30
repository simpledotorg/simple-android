package org.simple.clinic.forgotpin.createnewpin

import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent

sealed class ForgotPinCreateNewEvent : UiEvent

data class LoggedInUserLoaded(val user: User) : ForgotPinCreateNewEvent()

data class CurrentFacilityLoaded(val facility: Facility) : ForgotPinCreateNewEvent()

data class PinValidated(val isValid: Boolean) : ForgotPinCreateNewEvent()

data class ForgotPinCreateNewPinTextChanged(val pin: String) : ForgotPinCreateNewEvent() {
  override val analyticsName = "Forgot PIN:Create new PIN:Text Changed"
}

object ForgotPinCreateNewPinSubmitClicked : ForgotPinCreateNewEvent() {
  override val analyticsName = "Forgot PIN:Create new PIN:Submit Clicked"
}

