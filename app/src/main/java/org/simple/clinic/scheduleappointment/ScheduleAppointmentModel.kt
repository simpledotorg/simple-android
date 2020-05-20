package org.simple.clinic.scheduleappointment

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.util.UserClock
import java.util.UUID

@Parcelize
data class ScheduleAppointmentModel(
    val patientUuid: UUID,
    val potentialAppointmentDates: List<PotentialAppointmentDate>,
    val selectedAppointmentDate: PotentialAppointmentDate?,
    val appointmentFacility: Facility?
) : Parcelable {

  companion object {
    fun create(
        patientUuid: UUID,
        timeToAppointments: List<TimeToAppointment>,
        userClock: UserClock
    ): ScheduleAppointmentModel {
      val potentialAppointmentDates = generatePotentialAppointmentDatesForScheduling(timeToAppointments, userClock)

      return ScheduleAppointmentModel(
          patientUuid = patientUuid,
          potentialAppointmentDates = potentialAppointmentDates,
          selectedAppointmentDate = null,
          appointmentFacility = null
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

  val hasLoadedAppointmentFacility: Boolean
    get() = appointmentFacility != null

  fun appointmentDateSelected(potentialAppointmentDate: PotentialAppointmentDate): ScheduleAppointmentModel {
    return copy(selectedAppointmentDate = potentialAppointmentDate)
  }

  fun appointmentFacilitySelected(facility: Facility): ScheduleAppointmentModel {
    return copy(appointmentFacility = facility)
  }
}
