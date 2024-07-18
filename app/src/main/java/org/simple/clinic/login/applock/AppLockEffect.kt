package org.simple.clinic.login.applock

sealed class AppLockEffect

data object UnlockOnAuthentication : AppLockEffect()

data object LoadLoggedInUser : AppLockEffect()

data object LoadCurrentFacility : AppLockEffect()

data object LoadDataProtectionConsent : AppLockEffect()

sealed class AppLockViewEffect : AppLockEffect()

data object MarkDataProtectionConsent : AppLockEffect()

data object ExitApp : AppLockViewEffect()

data object ShowConfirmResetPinDialog : AppLockViewEffect()

data object RestorePreviousScreen : AppLockViewEffect()

data object ShowDataProtectionConsentDialog : AppLockViewEffect()
