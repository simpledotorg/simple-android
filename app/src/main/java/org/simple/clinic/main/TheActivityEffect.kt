package org.simple.clinic.main

import org.simple.clinic.navigation.v2.History

sealed class TheActivityEffect

data object LoadInitialScreenInfo : TheActivityEffect()

data object ClearLockAfterTimestamp : TheActivityEffect()

data object ListenForUserVerifications : TheActivityEffect()

data object ShowUserLoggedOutOnOtherDeviceAlert : TheActivityEffect()

data object ListenForUserUnauthorizations : TheActivityEffect()

data object RedirectToLoginScreen : TheActivityEffect()

data object ListenForUserDisapprovals : TheActivityEffect()

data object ClearPatientData : TheActivityEffect()

data object ShowAccessDeniedScreen : TheActivityEffect()

data class SetCurrentScreenHistory(val history: History) : TheActivityEffect()
