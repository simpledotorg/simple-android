package org.simple.clinic.registration.phone.loggedout

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class LoggedOutOfDeviceUpdate : Update<LoggedOutOfDeviceModel, LoggedOutOfDeviceEvent, LoggedOutOfDeviceEffect> {
  override fun update(model: LoggedOutOfDeviceModel, event: LoggedOutOfDeviceEvent): Next<LoggedOutOfDeviceModel, LoggedOutOfDeviceEffect> {
    return noChange()
  }
}
