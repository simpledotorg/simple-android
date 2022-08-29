package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class NextAppointmentUpdate : Update<NextAppointmentModel, NextAppointmentEvent, NextAppointmentEffect> {

  override fun update(model: NextAppointmentModel, event: NextAppointmentEvent): Next<NextAppointmentModel, NextAppointmentEffect> {
    return when (event) {
      is NextAppointmentPatientProfileLoaded -> next(model.nextAppointmentPatientProfileLoaded(event.nextAppointmentPatientProfile))
      RefreshAppointment -> dispatch(LoadNextAppointmentPatientProfile(model.patientUuid))
    }
  }
}
