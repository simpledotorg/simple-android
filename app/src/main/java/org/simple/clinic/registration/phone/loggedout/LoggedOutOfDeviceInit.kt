package org.simple.clinic.registration.phone.loggedout

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class LoggedOutOfDeviceInit : Init<LoggedOutOfDeviceModel, LoggedOutOfDeviceEffect> {
  override fun init(model: LoggedOutOfDeviceModel): First<LoggedOutOfDeviceModel, LoggedOutOfDeviceEffect> {
    return first(model, LogoutUser)
  }
}
