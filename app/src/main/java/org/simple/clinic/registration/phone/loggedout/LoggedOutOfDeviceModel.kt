package org.simple.clinic.registration.phone.loggedout

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.user.UserSession.LogoutResult

@Parcelize
data class LoggedOutOfDeviceModel(
    val logoutResult: LogoutResult?
) : Parcelable {

  companion object {
    fun create() = LoggedOutOfDeviceModel(
        logoutResult = null
    )
  }

  val hasLogoutResult: Boolean
    get() = logoutResult != null

  fun userLoggedOut(logoutResult: LogoutResult): LoggedOutOfDeviceModel {
    return copy(logoutResult = logoutResult)
  }
}
