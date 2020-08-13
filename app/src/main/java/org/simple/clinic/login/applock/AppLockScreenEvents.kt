package org.simple.clinic.login.applock

import org.simple.clinic.widgets.UiEvent

class AppLockScreenCreated : UiEvent

class AppLockForgotPinClicked : UiEvent {
  override val analyticsName = "App Lock:Forgot PIN Clicked"
}

class AppLockPinAuthenticated : UiEvent {
  override val analyticsName = "App Lock:PIN authenticated"
}
