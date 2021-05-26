package org.simple.clinic.enterotp

sealed class EnterOtpEffect

object LoadUser : EnterOtpEffect()

object ClearPin : EnterOtpEffect()

object TriggerSync : EnterOtpEffect()

object ClearLoginEntry : EnterOtpEffect()

data class LoginUser(val otp: String) : EnterOtpEffect()

object GoBack : EnterOtpEffect()

object ListenForUserBackgroundVerification : EnterOtpEffect()

object RequestLoginOtp : EnterOtpEffect()

object ShowSmsSentMessage : EnterOtpEffect()
