package org.simple.clinic.registration.name

import org.simple.clinic.widgets.UiEvent

class RegistrationFullNameScreenCreated : UiEvent

class RegistrationFullNameDoneClicked : UiEvent {
  override val analyticsName = "Registration:Name Entry:Done Clicked"
}
