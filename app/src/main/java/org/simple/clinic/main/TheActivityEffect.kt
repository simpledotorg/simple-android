package org.simple.clinic.main

sealed class TheActivityEffect

object LoadInitialScreenInfo : TheActivityEffect()

object ClearLockAfterTimestamp : TheActivityEffect()

object ShowAppLockScreen : TheActivityEffect()

object ListenForUserVerifications : TheActivityEffect()

object ShowUserLoggedOutOnOtherDeviceAlert : TheActivityEffect()

object ListenForUserUnauthorizations : TheActivityEffect()

object RedirectToLoginScreen : TheActivityEffect()

object ListenForUserDisapprovals: TheActivityEffect()

object ClearPatientData: TheActivityEffect()

object ShowAccessDeniedScreen: TheActivityEffect()

object ShowHomeScreen: TheActivityEffect()

object ShowForgotPinCreatePinScreen: TheActivityEffect()
