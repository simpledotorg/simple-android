package org.simple.clinic.forgotpin.confirmpin

import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent

sealed class ForgotPinConfirmPinEvent : UiEvent

data class LoggedInUserLoaded(val user: User) : ForgotPinConfirmPinEvent()

data class CurrentFacilityLoaded(val facility: Facility) : ForgotPinConfirmPinEvent()
