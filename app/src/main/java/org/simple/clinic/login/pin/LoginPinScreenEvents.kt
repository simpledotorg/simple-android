package org.simple.clinic.login.pin

import org.simple.clinic.widgets.UiEvent

// This event does not need an analytics name because it is
// part of the old, deprecated login flow and will be removed
data class LoginPinOtpReceived(val otp: String) : UiEvent
