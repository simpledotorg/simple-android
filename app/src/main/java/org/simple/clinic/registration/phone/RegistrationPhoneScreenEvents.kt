package org.simple.clinic.registration.phone

import org.simple.clinic.widgets.UiEvent

data class RegistrationPhoneNumberTextChanged(val phoneNumber: String) : UiEvent

class RegistrationPhoneDoneClicked : UiEvent

class RegistrationPhoneScreenCreated : UiEvent
