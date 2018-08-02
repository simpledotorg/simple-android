package org.simple.clinic.registration.confirmpin

import org.simple.clinic.widgets.UiEvent

class RegistrationConfirmPinScreenCreated : UiEvent

data class RegistrationConfirmPinTextChanged(val confirmPin: String) : UiEvent

class RegistrationConfirmPinDoneClicked : UiEvent
