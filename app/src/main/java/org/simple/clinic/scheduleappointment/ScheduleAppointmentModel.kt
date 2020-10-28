package org.simple.clinic.scheduleappointment

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecord
import org.simple.clinic.util.UserClock
import java.util.UUID
import org.simple.clinic.newentry.ButtonState as ButtonState
import org.simple.clinic.scheduleappointment.ButtonState as NextButtonState

@Parcelize
data class ScheduleAppointmentModel(
    val patientUuid: UUID,
    val potentialAppointmentDates: List<PotentialAppointmentDate>,
    val selectedAppointmentDate: PotentialAppointmentDate?,
    val appointmentFacility: Facility?,
    val doneButtonState: ButtonState,
    val teleconsultRecord: TeleconsultRecord?,
    val nextButtonState: NextButtonState
) : Parcelable {

  companion object {
    fun create(
        patientUuid: UUID,
        timeToAppointments: List<TimeToAppointment>,
        userClock: UserClock,
        doneButtonState: ButtonState,
        nextButtonState: NextButtonState
    ): ScheduleAppointmentModel {
      val potentialAppointmentDates = generatePotentialAppointmentDatesForScheduling(timeToAppointments, userClock)

      return ScheduleAppointmentModel(
          patientUuid = patientUuid,
          potentialAppointmentDates = potentialAppointmentDates,
          selectedAppointmentDate = null,
          appointmentFacility = null,
          doneButtonState = doneButtonState,
          teleconsultRecord = null,
          nextButtonState = nextButtonState
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

  val hasTeleconsultRecord: Boolean
    get() = teleconsultRecord != null

  val requesterCompletionStatus = teleconsultRecord?.teleconsultRequestInfo?.requesterCompletionStatus

  fun appointmentDateSelected(potentialAppointmentDate: PotentialAppointmentDate): ScheduleAppointmentModel {
    return copy(selectedAppointmentDate = potentialAppointmentDate)
  }

  fun appointmentFacilitySelected(facility: Facility): ScheduleAppointmentModel {
    return copy(appointmentFacility = facility)
  }

  fun doneButtonStateChanged(doneButtonState: ButtonState): ScheduleAppointmentModel {
    return copy(doneButtonState = doneButtonState)
  }

  fun teleconsultRecordLoaded(teleconsultRecord: TeleconsultRecord?): ScheduleAppointmentModel {
    return copy(teleconsultRecord = teleconsultRecord)
  }
}
