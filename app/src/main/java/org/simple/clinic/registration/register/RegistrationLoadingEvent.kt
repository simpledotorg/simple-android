package org.simple.clinic.registration.register

import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent

sealed class RegistrationLoadingEvent : UiEvent

data class RegistrationDetailsLoaded(val user: User) : RegistrationLoadingEvent()

data class UserRegistrationCompleted(val result: RegisterUserResult) : RegistrationLoadingEvent()

object RegisterErrorRetryClicked : RegistrationLoadingEvent() {
  override val analyticsName: String = "Registration:Loading:Retry Clicked"
}

data class ConvertedRegistrationEntryToUserDetails(val user: User): RegistrationLoadingEvent()
