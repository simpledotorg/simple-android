package org.simple.clinic.registration.phone

import org.simple.clinic.widgets.UiEvent

sealed class RegistrationPhoneEvent: UiEvent

data class RegistrationPhoneNumberTextChanged(val phoneNumber: String) : RegistrationPhoneEvent() {
  override val analyticsName = "Registration:Phone Entry:Phone Number Text Changed"
}
