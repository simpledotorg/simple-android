package org.simple.clinic.login.applock

sealed class AppLockEffect

object RestorePreviousScreen : AppLockEffect()

object UnlockOnAuthentication : AppLockEffect()

object LoadLoggedInUser : AppLockEffect()

object LoadCurrentFacility : AppLockEffect()

sealed class AppLockViewEffect : AppLockEffect()

object ExitApp : AppLockViewEffect()

object ShowConfirmResetPinDialog : AppLockViewEffect()
