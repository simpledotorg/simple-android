package org.simple.clinic.login.pin

import org.simple.clinic.widgets.UiEvent

data class PinTextChanged(val otpString: String) : UiEvent

class PinSubmitClicked : UiEvent

class PinScreenCreated : UiEvent
