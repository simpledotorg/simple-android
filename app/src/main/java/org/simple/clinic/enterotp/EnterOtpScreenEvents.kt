package org.simple.clinic.enterotp

import org.simple.clinic.widgets.UiEvent

data class EnterOtpSubmitted(val otp: String) : UiEvent {
  override val analyticsName = "Enter OTP Manually:OTP Submit Clicked"
}

class EnterOtpResendSmsClicked: UiEvent {
  override val analyticsName = "Enter OTP Manually:Resend SMS Clicked"
}
