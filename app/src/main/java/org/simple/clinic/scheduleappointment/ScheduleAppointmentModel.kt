package org.simple.clinic.scheduleappointment

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.util.UserClock

@Parcelize
data class ScheduleAppointmentModel(
    val potentialAppointmentDates: List<PotentialAppointmentDate>,
    val selectedAppointmentDate: CurrentAppointmentDateHolder?
) : Parcelable {

  companion object {
    fun create(
        timeToAppointments: List<TimeToAppointment>,
        userClock: UserClock
    ): ScheduleAppointmentModel {
      val potentialAppointmentDates = generatePotentialAppointmentDatesForScheduling(timeToAppointments, userClock)

      return ScheduleAppointmentModel(
          potentialAppointmentDates = potentialAppointmentDates,
          selectedAppointmentDate = null
      )
    }

    private fun generatePotentialAppointmentDatesForScheduling(
        timeToAppointments: List<TimeToAppointment>,
        clock: UserClock
    ): List<PotentialAppointmentDate> {
      return PotentialAppointmentDate.from(timeToAppointments, clock)
          .distinctBy(PotentialAppointmentDate::scheduledFor)
          .sorted()
    }
  }

  val hasLoadedAppointmentDate: Boolean
    get() = selectedAppointmentDate != null

  fun appointmentDateSelected(potentialAppointmentDate: PotentialAppointmentDate): ScheduleAppointmentModel {
    return copy(selectedAppointmentDate = CurrentAppointmentDateHolder(potentialAppointmentDate))
  }
}
