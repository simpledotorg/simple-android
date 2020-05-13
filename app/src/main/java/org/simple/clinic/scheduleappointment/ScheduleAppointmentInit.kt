package org.simple.clinic.scheduleappointment

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class ScheduleAppointmentInit: Init<ScheduleAppointmentModel, ScheduleAppointmentEffect> {

  override fun init(model: ScheduleAppointmentModel): First<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    return first(model)
  }
}
