package org.simple.clinic.forgotpin.createnewpin

import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent

sealed class ForgotPinCreateNewEvent : UiEvent

data class LoggedInUserLoaded(val user: User) : ForgotPinCreateNewEvent()

data class CurrentFacilityLoaded(val facility: Facility) : ForgotPinCreateNewEvent()
