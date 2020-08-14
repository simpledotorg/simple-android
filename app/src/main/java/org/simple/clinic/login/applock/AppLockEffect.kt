package org.simple.clinic.login.applock

sealed class AppLockEffect

object ExitApp : AppLockEffect()

object ShowConfirmResetPinDialog : AppLockEffect()

object RestorePreviousScreen : AppLockEffect()

object UnlockOnAuthentication : AppLockEffect()

object LoadLoggedInUser : AppLockEffect()

object LoadCurrentFacility : AppLockEffect()
