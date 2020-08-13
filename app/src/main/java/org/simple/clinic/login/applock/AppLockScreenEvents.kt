package org.simple.clinic.login.applock

import org.simple.clinic.widgets.UiEvent

class AppLockScreenCreated : UiEvent

class AppLockPinAuthenticated : UiEvent {
  override val analyticsName = "App Lock:PIN authenticated"
}
