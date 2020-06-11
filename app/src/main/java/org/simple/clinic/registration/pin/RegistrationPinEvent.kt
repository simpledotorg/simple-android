package org.simple.clinic.registration.pin

import org.simple.clinic.widgets.UiEvent

sealed class RegistrationPinEvent: UiEvent

object CurrentOngoingEntrySaved: RegistrationPinEvent()
