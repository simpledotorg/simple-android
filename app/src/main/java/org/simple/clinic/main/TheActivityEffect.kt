package org.simple.clinic.main

import java.time.Instant

sealed class TheActivityEffect

object LoadAppLockInfo : TheActivityEffect()

object ClearLockAfterTimestamp : TheActivityEffect()

object ShowAppLockScreen : TheActivityEffect()

data class UpdateLockTimestamp(val lockAt: Instant) : TheActivityEffect()

object ListenForUserVerifications : TheActivityEffect()

object ShowUserLoggedOutOnOtherDeviceAlert : TheActivityEffect()

object ListenForUserUnauthorizations : TheActivityEffect()

object RedirectToLoginScreen : TheActivityEffect()

object ListenForUserDisapprovals: TheActivityEffect()
