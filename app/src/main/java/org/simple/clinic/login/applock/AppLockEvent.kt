package org.simple.clinic.login.applock

import org.simple.clinic.widgets.UiEvent

sealed class AppLockEvent : UiEvent

object AppLockBackClicked : AppLockEvent() {
  override val analyticsName = "App Lock:Back Clicked"
}

object AppLockForgotPinClicked : AppLockEvent() {
  override val analyticsName = "App Lock:Forgot PIN Clicked"
}

object UnlockApp : AppLockEvent() {
  override val analyticsName = "App Lock:Unlocked"
}

object AppLockPinAuthenticated : AppLockEvent() {
  override val analyticsName = "App Lock:PIN authenticated"
}
