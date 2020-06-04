package org.simple.clinic.registration.phone

import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent

sealed class RegistrationPhoneEvent: UiEvent

data class RegistrationPhoneNumberTextChanged(val phoneNumber: String) : RegistrationPhoneEvent() {
  override val analyticsName = "Registration:Phone Entry:Phone Number Text Changed"
}

data class CurrentRegistrationEntryLoaded(val entry: Optional<OngoingRegistrationEntry>): RegistrationPhoneEvent()
