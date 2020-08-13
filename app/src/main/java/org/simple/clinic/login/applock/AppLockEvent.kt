package org.simple.clinic.login.applock

import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User
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

data class LoggedInUserLoaded(val user: User) : AppLockEvent()

data class CurrentFacilityLoaded(val facility: Facility) : AppLockEvent()
