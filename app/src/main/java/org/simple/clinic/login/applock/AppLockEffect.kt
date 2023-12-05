package org.simple.clinic.login.applock

sealed class AppLockEffect

object UnlockOnAuthentication : AppLockEffect()

object LoadLoggedInUser : AppLockEffect()

object LoadCurrentFacility : AppLockEffect()

object LoadDataProtectionConsent : AppLockEffect()

sealed class AppLockViewEffect : AppLockEffect()

object MarkDataProtectionConsent : AppLockEffect()

object ExitApp : AppLockViewEffect()

object ShowConfirmResetPinDialog : AppLockViewEffect()

object RestorePreviousScreen : AppLockViewEffect()

object ShowDataProtectionConsentDialog : AppLockViewEffect()
