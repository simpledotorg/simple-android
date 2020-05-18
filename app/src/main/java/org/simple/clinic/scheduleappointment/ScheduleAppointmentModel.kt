package org.simple.clinic.scheduleappointment

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.util.UserClock

@Parcelize
class ScheduleAppointmentModel(
    val potentialAppointmentDates: List<PotentialAppointmentDate>
) : Parcelable {

  companion object {
    fun create(
        timeToAppointments: List<TimeToAppointment>,
        userClock: UserClock
    ): ScheduleAppointmentModel {
      return ScheduleAppointmentModel(
          potentialAppointmentDates = generatePotentialAppointmentDatesForScheduling(timeToAppointments, userClock)
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
}
