package org.simple.clinic.registration.name

import org.simple.clinic.widgets.UiEvent

data class RegistrationFullNameTextChanged(val fullName: String) : UiEvent

class RegistrationFullNameNextClicked : UiEvent

class RegistrationFullNameScreenCreated : UiEvent
