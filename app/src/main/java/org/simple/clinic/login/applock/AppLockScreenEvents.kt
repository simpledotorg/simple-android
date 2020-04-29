package org.simple.clinic.login.applock

import org.simple.clinic.widgets.UiEvent

class AppLockScreenCreated : UiEvent

class AppLockBackClicked : UiEvent {
  override val analyticsName = "App Lock:Back Clicked"
}

class AppLockForgotPinClicked: UiEvent {
  override val analyticsName = "App Lock:Forgot PIN Clicked"
}

class AppLockPinAuthenticated : UiEvent {
  override val analyticsName = "App Lock:PIN authenticated"
}
