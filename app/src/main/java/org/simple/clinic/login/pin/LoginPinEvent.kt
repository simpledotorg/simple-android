package org.simple.clinic.login.pin

import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.widgets.UiEvent

sealed class LoginPinEvent : UiEvent

data class OngoingLoginEntryLoaded(val ongoingLoginEntry: OngoingLoginEntry) : LoginPinEvent()
