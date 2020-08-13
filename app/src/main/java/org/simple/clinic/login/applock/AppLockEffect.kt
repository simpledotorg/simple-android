package org.simple.clinic.login.applock

sealed class AppLockEffect

object ExitApp : AppLockEffect()

object ShowConfirmResetPinDialog : AppLockEffect()
