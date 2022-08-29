package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class NextAppointmentInit : Init<NextAppointmentModel, NextAppointmentEffect> {

  override fun init(model: NextAppointmentModel): First<NextAppointmentModel, NextAppointmentEffect> {
    val effects = mutableSetOf<NextAppointmentEffect>()
    if (!model.hasNextAppointmentPatientProfile) {
      effects.add(LoadNextAppointmentPatientProfile(model.patientUuid))
    }

    return first(model, effects)
  }
}
