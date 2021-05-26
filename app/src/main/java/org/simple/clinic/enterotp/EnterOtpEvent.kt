package org.simple.clinic.enterotp

import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.activateuser.ActivateUser
import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent

sealed class EnterOtpEvent : UiEvent

data class UserLoaded(val user: User) : EnterOtpEvent()

data class EnterOtpSubmitted(val otp: String) : EnterOtpEvent() {
  override val analyticsName = "Enter OTP Manually:OTP Submit Clicked"
}

data class LoginUserCompleted(val result: LoginResult) : EnterOtpEvent()

object UserVerifiedInBackground : EnterOtpEvent()

data class RequestLoginOtpCompleted(val result: ActivateUser.Result) : EnterOtpEvent()

class EnterOtpResendSmsClicked : EnterOtpEvent() {
  override val analyticsName = "Enter OTP Manually:Resend SMS Clicked"
}
