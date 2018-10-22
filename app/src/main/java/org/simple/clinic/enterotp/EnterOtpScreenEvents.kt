package org.simple.clinic.enterotp

import org.simple.clinic.widgets.UiEvent

class EnterOtpBackClicked : UiEvent {
  override val analyticsName = "Enter OTP:Back Clicked"
}

data class EnterOtpSubmitted(val otp: String) : UiEvent {
  override val analyticsName = "Enter OTP Manually:OTP Submit Clicked"
}

data class EnterOtpTextChanges(val otp: String) : UiEvent {
  override val analyticsName = "Enter OTP Manually:OTP Text Changed"
}

class EnterOtpResendSmsClicked: UiEvent {
  override val analyticsName = "Enter OTP Manually:Resend SMS Clicked"
}
