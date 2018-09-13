package org.simple.clinic.login.applock

import org.simple.clinic.widgets.UiEvent

class AppLockScreenCreated : UiEvent

data class AppLockScreenPinTextChanged(val pin: String) : UiEvent {
  override val analyticsName = "App Lock:Pin Text Changed"
}

class AppLockScreenSubmitClicked : UiEvent {
  override val analyticsName = "App Lock:Submit Clicked"
}

class AppLockScreenBackClicked : UiEvent {
  override val analyticsName = "App Lock:Back Clicked"
}

class AppLockFacilityClicked : UiEvent {
  override val analyticsName = "App Lock:Facility Clicked"
}

class AppLockForgotPinClicked: UiEvent {
  override val analyticsName = "App Lock:Forgot PIN Clicked"
}
