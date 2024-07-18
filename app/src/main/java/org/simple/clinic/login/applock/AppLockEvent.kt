package org.simple.clinic.login.applock

import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent

sealed class AppLockEvent : UiEvent

data object AppLockBackClicked : AppLockEvent() {
  override val analyticsName = "App Lock:Back Clicked"
}

data object AppLockForgotPinClicked : AppLockEvent() {
  override val analyticsName = "App Lock:Forgot PIN Clicked"
}

data object UnlockApp : AppLockEvent() {
  override val analyticsName = "App Lock:Unlocked"
}

data object AppLockPinAuthenticated : AppLockEvent() {
  override val analyticsName = "App Lock:PIN authenticated"
}

data class LoggedInUserLoaded(val user: User) : AppLockEvent()

data class CurrentFacilityLoaded(val facility: Facility) : AppLockEvent()

data class DataProtectionConsentLoaded(val hasUserConsentedToDataProtection: Boolean) : AppLockEvent()

data object FinishedMarkingDataProtectionConsent : AppLockEvent()

data object AcceptDataProtectionConsentClicked : AppLockEvent() {
  override val analyticsName: String = "App Lock:Accept Data Protection Consent Clicked"
}
