package org.simple.clinic.registration.confirmpin

import org.simple.clinic.widgets.UiEvent

data class RegistrationConfirmPinTextChanged(val confirmPin: String) : UiEvent

class RegistrationConfirmPinNextClicked : UiEvent

class RegistrationConfirmPinScreenCreated : UiEvent
