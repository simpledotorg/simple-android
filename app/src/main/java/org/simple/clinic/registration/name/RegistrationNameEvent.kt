package org.simple.clinic.registration.name

import org.simple.clinic.widgets.UiEvent

sealed class RegistrationNameEvent: UiEvent

data class RegistrationFullNameTextChanged(val fullName: String) : RegistrationNameEvent() {
  override val analyticsName = "Registration:Name Entry:Name Text Changed"
}
