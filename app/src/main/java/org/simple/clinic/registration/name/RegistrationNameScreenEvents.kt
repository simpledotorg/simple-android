package org.simple.clinic.registration.name

import org.simple.clinic.widgets.UiEvent

class RegistrationFullNameScreenCreated : UiEvent

data class RegistrationFullNameTextChanged(val fullName: String) : UiEvent {
  override val analyticsName = "Registration:Name Entry:Name Text Changed"
}

class RegistrationFullNameDoneClicked : UiEvent {
  override val analyticsName = "Registration:Name Entry:Done Clicked"
}
