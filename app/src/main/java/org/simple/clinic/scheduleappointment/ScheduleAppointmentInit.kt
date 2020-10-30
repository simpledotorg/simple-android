package org.simple.clinic.scheduleappointment

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class ScheduleAppointmentInit : Init<ScheduleAppointmentModel, ScheduleAppointmentEffect> {

  override fun init(model: ScheduleAppointmentModel): First<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    val effects = mutableSetOf<ScheduleAppointmentEffect>()

    if (!model.hasLoadedAppointmentDate) {
      effects.add(LoadDefaultAppointmentDate)
    }

    if (!model.hasLoadedAppointmentFacility) {
      effects.add(LoadAppointmentFacilities(model.patientUuid))
    }

    if (!model.hasTeleconsultRecord)
      effects.add(LoadTeleconsultRecord(model.patientUuid))

    return first(model, effects)
  }
}
