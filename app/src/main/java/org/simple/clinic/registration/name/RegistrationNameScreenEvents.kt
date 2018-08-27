package org.simple.clinic.registration.name

import org.simple.clinic.widgets.UiEvent

class RegistrationFullNameScreenCreated : UiEvent {
  override val analyticsName = "Registration:Name Entry:Show Screen"
}

data class RegistrationFullNameTextChanged(val fullName: String) : UiEvent {
  override val analyticsName = "Registration:Name Entry:Name Text Changed"
}

class RegistrationFullNameDoneClicked : UiEvent {
  override val analyticsName = "Registration:Name Entry:Done Clicked"
}
