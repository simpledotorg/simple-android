package org.simple.clinic.registration.phone

import org.simple.clinic.widgets.UiEvent

data class RegistrationPhoneNumberTextChanged(val phoneNumber: String) : UiEvent {
  override val analyticsName = "Registration:Phone Entry:Phone Number Text Changed"
}

class RegistrationPhoneDoneClicked : UiEvent {
  override val analyticsName = "Registration:Phone Entry:Done Clicked"
}

class RegistrationPhoneScreenCreated : UiEvent
