package org.simple.clinic.enterotp

import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent

sealed class EnterOtpEvent: UiEvent

data class UserLoaded(val user: User): EnterOtpEvent()
