package org.simple.clinic.registration.phone.loggedout

import org.simple.clinic.user.UserSession.LogoutResult
import org.simple.clinic.widgets.UiEvent

sealed class LoggedOutOfDeviceEvent : UiEvent

data class UserLoggedOut(val logoutResult: LogoutResult) : LoggedOutOfDeviceEvent()
