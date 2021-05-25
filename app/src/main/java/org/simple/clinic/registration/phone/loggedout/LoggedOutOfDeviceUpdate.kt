package org.simple.clinic.registration.phone.loggedout

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserSession.LogoutResult.Success

class LoggedOutOfDeviceUpdate : Update<LoggedOutOfDeviceModel, LoggedOutOfDeviceEvent, LoggedOutOfDeviceEffect> {
  override fun update(
      model: LoggedOutOfDeviceModel,
      event: LoggedOutOfDeviceEvent
  ): Next<LoggedOutOfDeviceModel, LoggedOutOfDeviceEffect> {
    return when (event) {
      is UserLoggedOut -> userLoggedOut(model, event)
    }
  }

  private fun userLoggedOut(
      model: LoggedOutOfDeviceModel,
      event: UserLoggedOut
  ): Next<LoggedOutOfDeviceModel, LoggedOutOfDeviceEffect> {
    val updatedModel = model.userLoggedOut(event.logoutResult)
    return if (event.logoutResult == Success) {
      next(updatedModel)
    } else {
      val failure = event.logoutResult as UserSession.LogoutResult.Failure
      next(updatedModel, ThrowError(failure.cause))
    }
  }
}
