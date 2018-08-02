package org.simple.clinic.registration.pin

import org.simple.clinic.widgets.UiEvent

class RegistrationPinScreenCreated : UiEvent

data class RegistrationPinTextChanged(val pin: String) : UiEvent

class RegistrationPinDoneClicked : UiEvent
