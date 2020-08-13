package org.simple.clinic.login.applock

import org.simple.clinic.widgets.UiEvent

sealed class AppLockEvent : UiEvent

object AppLockBackClicked : AppLockEvent() {
  override val analyticsName = "App Lock:Back Clicked"
}
