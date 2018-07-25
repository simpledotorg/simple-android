package org.simple.clinic.login.phone

import org.simple.clinic.widgets.UiEvent

data class LoginPhoneNumberScreenCreated(val otp: String) : UiEvent

data class LoginPhoneNumberTextChanged(val phoneNumber: String) : UiEvent

class LoginPhoneNumberSubmitClicked : UiEvent
