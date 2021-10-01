package org.simple.clinic.main

import org.simple.clinic.navigation.v2.History

sealed class TheActivityEffect

object LoadInitialScreenInfo : TheActivityEffect()

object ClearLockAfterTimestamp : TheActivityEffect()

object ListenForUserVerifications : TheActivityEffect()

object ShowUserLoggedOutOnOtherDeviceAlert : TheActivityEffect()

object ListenForUserUnauthorizations : TheActivityEffect()

object RedirectToLoginScreen : TheActivityEffect()

object ListenForUserDisapprovals : TheActivityEffect()

object ClearPatientData : TheActivityEffect()

object ShowAccessDeniedScreen : TheActivityEffect()

data class SetCurrentScreenHistory(val history: History) : TheActivityEffect()
