package org.simple.clinic.login.pin

import org.simple.clinic.widgets.UiEvent

data class PinTextChanged(val pin: String) : UiEvent

class PinSubmitClicked : UiEvent

class PinScreenCreated : UiEvent

class PinBackClicked : UiEvent

data class LoginPinOtpReceived(val otp: String): UiEvent
