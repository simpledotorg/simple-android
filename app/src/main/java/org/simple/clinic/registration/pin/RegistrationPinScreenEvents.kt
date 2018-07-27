package org.simple.clinic.registration.pin

import org.simple.clinic.widgets.UiEvent

data class RegistrationPinTextChanged(val pin: String) : UiEvent

class RegistrationPinNextClicked : UiEvent