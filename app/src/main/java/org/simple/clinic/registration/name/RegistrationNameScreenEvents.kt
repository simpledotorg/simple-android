package org.simple.clinic.registration.name

import org.simple.clinic.widgets.UiEvent

class RegistrationFullNameScreenCreated : UiEvent

data class RegistrationFullNameTextChanged(val fullName: String) : UiEvent

class RegistrationFullNameDoneClicked : UiEvent
