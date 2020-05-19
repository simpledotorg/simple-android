package org.simple.clinic.scheduleappointment

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class ScheduleAppointmentUpdate : Update<ScheduleAppointmentModel, ScheduleAppointmentEvent, ScheduleAppointmentEffect> {

  override fun update(
      model: ScheduleAppointmentModel,
      event: ScheduleAppointmentEvent
  ): Next<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    return when(event) {
      is DefaultAppointmentDateLoaded -> next(model.appointmentDateSelected(event.potentialAppointmentDate))
      is AppointmentDateIncremented -> {
        val currentScheduledDate = model.selectedAppointmentDate!!.date

        val nextPotentialAppointmentDate = model.potentialAppointmentDates
            .find { it > currentScheduledDate }
            ?: throw RuntimeException("Cannot find configured appointment date after $currentScheduledDate")

        next(model.appointmentDateSelected(nextPotentialAppointmentDate))
      }
    }
  }
}
