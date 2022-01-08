package org.simple.clinic.scheduleappointment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.newentry.ButtonState
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.summary.AppointmentSheetOpenedFrom
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecord
import org.simple.clinic.util.UserClock
import java.util.UUID
import org.simple.clinic.scheduleappointment.ButtonState as NextButtonState

@Parcelize
data class ScheduleAppointmentModel(
    val patientUuid: UUID,
    val potentialAppointmentDateModel: PotentialAppointmentDateModel,
    val appointmentFacility: Facility?,
    val doneButtonState: ButtonState,
    val teleconsultRecord: TeleconsultRecord?,
    val nextButtonState: org.simple.clinic.scheduleappointment.ButtonState,
    val openedFrom: AppointmentSheetOpenedFrom
) : Parcelable {

  companion object {
    fun create(
        patientUuid: UUID,
        timeToAppointments: List<TimeToAppointment>,
        userClock: UserClock,
        doneButtonState: ButtonState,
        nextButtonState: org.simple.clinic.scheduleappointment.ButtonState,
        openedFrom: AppointmentSheetOpenedFrom
    ): ScheduleAppointmentModel {
      val potentialAppointmentDates = generatePotentialAppointmentDatesForScheduling(timeToAppointments, userClock)
      val potientialAppointmentDateModel = PotentialAppointmentDateModel
          .create(potentialAppointmentDates = potentialAppointmentDates)

      return ScheduleAppointmentModel(
          patientUuid = patientUuid,
          potentialAppointmentDateModel = potientialAppointmentDateModel,
          appointmentFacility = null,
          doneButtonState = doneButtonState,
          teleconsultRecord = null,
          nextButtonState = nextButtonState,
          openedFrom = openedFrom
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
    get() = potentialAppointmentDateModel.selectedAppointmentDate != null

  val hasLoadedAppointmentFacility: Boolean
    get() = appointmentFacility != null

  val hasTeleconsultRecord: Boolean
    get() = teleconsultRecord != null

  val requesterCompletionStatus = teleconsultRecord?.teleconsultRequestInfo?.requesterCompletionStatus

  val potentialAppointmentDates: List<PotentialAppointmentDate>
    get() = potentialAppointmentDateModel.potentialAppointmentDates

  val selectedAppointmentDate: PotentialAppointmentDate?
    get() = potentialAppointmentDateModel.selectedAppointmentDate

  fun appointmentDateSelected(potentialAppointmentDate: PotentialAppointmentDate): ScheduleAppointmentModel {
    return copy(potentialAppointmentDateModel = potentialAppointmentDateModel.selectedAppointmentDate(potentialAppointmentDate))
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

  fun nextButtonStateChanged(nextButtonState: NextButtonState): ScheduleAppointmentModel {
    return copy(nextButtonState = nextButtonState)
  }
}
