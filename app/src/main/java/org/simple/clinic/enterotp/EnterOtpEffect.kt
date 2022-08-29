package org.simple.clinic.enterotp

import org.simple.clinic.login.LoginResult

sealed class EnterOtpEffect

object LoadUser : EnterOtpEffect()

object TriggerSync : EnterOtpEffect()

object ClearLoginEntry : EnterOtpEffect()

data class LoginUser(val otp: String) : EnterOtpEffect()

object ListenForUserBackgroundVerification : EnterOtpEffect()

object RequestLoginOtp : EnterOtpEffect()

data class FailedLoginOtpAttempt(val result: LoginResult) : EnterOtpEffect()

object LoadOtpEntryProtectedStates : EnterOtpEffect()

object ResetOtpAttemptLimit : EnterOtpEffect()

sealed class EnterOtpViewEffect : EnterOtpEffect()

object ClearPin : EnterOtpViewEffect()

object GoBack : EnterOtpViewEffect()

object ShowSmsSentMessage : EnterOtpViewEffect()

object ShowNetworkError : EnterOtpViewEffect()

object ShowUnexpectedError : EnterOtpViewEffect()
