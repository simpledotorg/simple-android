package org.simple.clinic.forgotpin.confirmpin

import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent

sealed class ForgotPinConfirmPinEvent : UiEvent

data class LoggedInUserLoaded(val user: User) : ForgotPinConfirmPinEvent()
