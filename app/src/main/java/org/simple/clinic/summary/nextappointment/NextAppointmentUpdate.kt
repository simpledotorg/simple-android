package org.simple.clinic.summary.nextappointment

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class NextAppointmentUpdate : Update<NextAppointmentModel, NextAppointmentEvent, NextAppointmentEffect> {

  override fun update(model: NextAppointmentModel, event: NextAppointmentEvent): Next<NextAppointmentModel, NextAppointmentEffect> {
    return when (event) {
      is AppointmentLoaded -> next(model.appointmentLoaded(event.appointment))
      is PatientAndAssignedFacilityLoaded -> noChange()
    }
  }
}
