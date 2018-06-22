package org.simple.clinic.login.phone

import org.simple.clinic.widgets.UiEvent

data class PhoneNumberScreenCreated(val otp: String) : UiEvent

data class PhoneNumberTextChanged(val phoneNumber: String) : UiEvent

class PhoneNumberSubmitClicked : UiEvent
