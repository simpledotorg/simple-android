package org.simple.clinic.registration.phone.loggedout

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class LoggedOutOfDeviceInit : Init<LoggedOutOfDeviceModel, LoggedOutOfDeviceEffect> {
  override fun init(model: LoggedOutOfDeviceModel): First<LoggedOutOfDeviceModel, LoggedOutOfDeviceEffect> {
    return first(model)
  }
}
