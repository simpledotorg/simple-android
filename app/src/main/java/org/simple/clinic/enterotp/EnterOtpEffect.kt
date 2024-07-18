package org.simple.clinic.enterotp

import org.simple.clinic.login.LoginResult

sealed class EnterOtpEffect

data object LoadUser : EnterOtpEffect()

data object TriggerSync : EnterOtpEffect()

data object ClearLoginEntry : EnterOtpEffect()

data class LoginUser(val otp: String) : EnterOtpEffect()

data object ListenForUserBackgroundVerification : EnterOtpEffect()

data object RequestLoginOtp : EnterOtpEffect()

data class FailedLoginOtpAttempt(val result: LoginResult) : EnterOtpEffect()

data object LoadOtpEntryProtectedStates : EnterOtpEffect()

data object ResetOtpAttemptLimit : EnterOtpEffect()

sealed class EnterOtpViewEffect : EnterOtpEffect()

data object ClearPin : EnterOtpViewEffect()

data object GoBack : EnterOtpViewEffect()

data object ShowSmsSentMessage : EnterOtpViewEffect()

data object ShowNetworkError : EnterOtpViewEffect()

data object ShowUnexpectedError : EnterOtpViewEffect()
