package org.simple.clinic.scheduleappointment

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.newentry.ButtonState
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment.Days
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.NEXT_APPOINTMENT_ACTION_CLICK
import org.simple.clinic.util.daysTill
import java.time.LocalDate
import java.time.Period
import org.simple.clinic.scheduleappointment.ButtonState as NextButtonState

class ScheduleAppointmentUpdate(
    private val currentDate: LocalDate,
    private val defaulterAppointmentPeriod: Period
) : Update<ScheduleAppointmentModel, ScheduleAppointmentEvent, ScheduleAppointmentEffect> {

  override fun update(
      model: ScheduleAppointmentModel,
      event: ScheduleAppointmentEvent
  ): Next<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    return when (event) {
      is DefaultAppointmentDateLoaded -> next(model.appointmentDateSelected(event.potentialAppointmentDate))
      is AppointmentDateIncremented -> selectLaterAppointmentDate(model)
      is AppointmentDateDecremented -> selectEarlierAppointmentDate(model)
      is AppointmentCalendarDateSelected -> selectExactAppointmentDate(event, model)
      ManuallySelectAppointmentDateClicked -> dispatch(ShowDatePicker(model.selectedAppointmentDate!!.scheduledFor))
      is AppointmentFacilitiesLoaded -> appointmentFacilityLoaded(model, event)
      is PatientFacilityChanged -> next(model.appointmentFacilitySelected(event.facility))
      is DoneClicked -> scheduleManualAppointment(model)
      is AppointmentScheduled -> next(model.doneButtonStateChanged(ButtonState.SAVED), CloseSheet)
      SchedulingSkipped -> schedulingSkipped(model)
      is PatientDefaulterStatusLoaded -> scheduleAutomaticAppointment(event, model)
      is TeleconsultRecordLoaded -> next(model.teleconsultRecordLoaded(event.teleconsultRecord))
      AppointmentScheduledForPatientFromNext -> next(model.nextButtonStateChanged(NextButtonState.SCHEDULED), GoToTeleconsultStatusSheet(model.teleconsultRecord!!.id))
      NextClicked -> scheduleManualAppointmentFromNext(model)
    }
  }

  private fun schedulingSkipped(model: ScheduleAppointmentModel): Next<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    val effect = if (model.openedFrom != NEXT_APPOINTMENT_ACTION_CLICK) {
      LoadPatientDefaulterStatus(model.patientUuid)
    } else {
      CloseSheetWithoutResult
    }

    return dispatch(effect)
  }

  private fun scheduleManualAppointmentFromNext(model: ScheduleAppointmentModel): Next<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    val effect = ScheduleAppointmentForPatientFromNext(
        patientUuid = model.patientUuid,
        scheduledForDate = model.selectedAppointmentDate!!.scheduledFor,
        scheduledAtFacility = model.appointmentFacility!!,
        type = Manual
    )

    return next(model.nextButtonStateChanged(NextButtonState.SCHEDULING), effect)
  }

  private fun appointmentFacilityLoaded(
      model: ScheduleAppointmentModel,
      event: AppointmentFacilitiesLoaded
  ): Next<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    val appointmentFacility = event.assignedFacility ?: event.currentFacility
    return next(model.appointmentFacilitySelected(appointmentFacility))
  }

  private fun selectLaterAppointmentDate(model: ScheduleAppointmentModel): Next<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    val currentSelectedAppointmentDate = model.selectedAppointmentDate!!
    val laterPotentialAppointmentDate = model.potentialAppointmentDates
        .find { potentialAppointmentDate -> potentialAppointmentDate > currentSelectedAppointmentDate }
        ?: throw RuntimeException("Cannot find configured appointment date after $currentSelectedAppointmentDate")

    return next(model.appointmentDateSelected(laterPotentialAppointmentDate))
  }

  private fun selectEarlierAppointmentDate(model: ScheduleAppointmentModel): Next<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    val currentSelectedAppointmentDate = model.selectedAppointmentDate!!
    val laterPotentialAppointmentDate = model.potentialAppointmentDates
        .findLast { potentialAppointmentDate -> potentialAppointmentDate < currentSelectedAppointmentDate }
        ?: throw RuntimeException("Cannot find configured appointment date after $currentSelectedAppointmentDate")

    return next(model.appointmentDateSelected(laterPotentialAppointmentDate))
  }

  private fun selectExactAppointmentDate(
      event: AppointmentCalendarDateSelected,
      model: ScheduleAppointmentModel
  ): Next<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    val userSelectedDate = event.selectedDate

    val matchingTimeToAppointmentFromPotentials = model.potentialAppointmentDates
        .find { potentialAppointmentDate -> potentialAppointmentDate.scheduledFor == userSelectedDate }
        ?.timeToAppointment

    val timeToAppointment = matchingTimeToAppointmentFromPotentials
        ?: Days(currentDate.daysTill(userSelectedDate))
    val potentialAppointmentDate = PotentialAppointmentDate(userSelectedDate, timeToAppointment)

    return next(model.appointmentDateSelected(potentialAppointmentDate))
  }

  private fun scheduleManualAppointment(model: ScheduleAppointmentModel): Next<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    val effect = ScheduleAppointmentForPatient(
        patientUuid = model.patientUuid,
        scheduledForDate = model.selectedAppointmentDate!!.scheduledFor,
        scheduledAtFacility = model.appointmentFacility!!,
        type = Manual
    )

    return next(model.doneButtonStateChanged(ButtonState.SAVING), effect)
  }

  private fun scheduleAutomaticAppointment(
      event: PatientDefaulterStatusLoaded,
      model: ScheduleAppointmentModel
  ): Next<ScheduleAppointmentModel, ScheduleAppointmentEffect> {
    val shouldAutomaticAppointmentBeScheduled = event.isPatientADefaulter

    val effect = if (shouldAutomaticAppointmentBeScheduled && model.openedFrom != NEXT_APPOINTMENT_ACTION_CLICK) {
      ScheduleAppointmentForPatient(
          patientUuid = model.patientUuid,
          scheduledForDate = currentDate + defaulterAppointmentPeriod,
          scheduledAtFacility = model.appointmentFacility!!,
          type = Automatic
      )
    } else {
      CloseSheet
    }

    return dispatch(effect)
  }
}
