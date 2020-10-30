package org.simple.clinic.scheduleappointment

import org.simple.clinic.facility.Facility
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.newentry.ButtonState
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecord
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus
import org.simple.clinic.util.ValueChangedCallback
import org.simple.clinic.scheduleappointment.ButtonState as NextButtonState

class ScheduleAppointmentUiRenderer(
    private val ui: ScheduleAppointmentUi
) : ViewRenderer<ScheduleAppointmentModel> {

  private val selectedDateChangedCallback = ValueChangedCallback<PotentialAppointmentDate>()
  private val facilityChangedCallback = ValueChangedCallback<Facility>()

  override fun render(model: ScheduleAppointmentModel) {
    if (model.hasLoadedAppointmentDate) {
      selectedDateChangedCallback.pass(model.selectedAppointmentDate!!) { selectedDate ->
        ui.updateScheduledAppointment(selectedDate.scheduledFor, selectedDate.timeToAppointment)
        toggleStateOfIncrementButton(selectedDate, model.potentialAppointmentDates)
        toggleStateOfDecrementButton(selectedDate, model.potentialAppointmentDates)
      }
    }

    if (model.hasLoadedAppointmentFacility) {
      facilityChangedCallback.pass(model.appointmentFacility!!) { facility ->
        ui.showPatientFacility(facility.name)
      }
    }

    manageDoneAndNextButtonVisibility(model)

    manageButtonState(model)
    manageNextButtonState(model)
  }

  private fun manageNextButtonState(model: ScheduleAppointmentModel) {
    if (model.nextButtonState == NextButtonState.SCHEDULING) {
      ui.showNextButtonProgress()
    } else
      ui.hideNextButtonProgress()
  }

  private fun manageDoneAndNextButtonVisibility(model: ScheduleAppointmentModel) {
    val teleconsultRecord = model.teleconsultRecord
    if (teleconsultRecord != null) {
      manageRequestCompletedStatus(model, teleconsultRecord)
    } else {
      ui.showDoneButton()
      ui.hideNextButton()
    }
  }

  private fun manageRequestCompletedStatus(model: ScheduleAppointmentModel, teleconsultRecord: TeleconsultRecord) {
    if (teleconsultRecord.teleconsultRequestInfo != null)
      manageNextButtonVisibility(model)
  }

  private fun manageNextButtonVisibility(model: ScheduleAppointmentModel) {
    when (model.requesterCompletionStatus) {
      TeleconsultStatus.StillWaiting, null -> {
        ui.showNextButton()
        ui.hideDoneButton()
      }
      is TeleconsultStatus.Unknown, TeleconsultStatus.Yes, TeleconsultStatus.No -> {
        ui.showDoneButton()
        ui.hideNextButton()
      }
    }
  }

  private fun manageButtonState(model: ScheduleAppointmentModel) {
    if (model.doneButtonState == ButtonState.SAVING) {
      ui.showProgress()
    } else
      ui.hideProgress()
  }

  private fun toggleStateOfIncrementButton(
      selectedAppointmentDate: PotentialAppointmentDate,
      allPotentialAppointmentDates: List<PotentialAppointmentDate>
  ) {
    val areLaterPotentialAppointmentsAvailable = selectedAppointmentDate < allPotentialAppointmentDates.last()

    ui.enableIncrementButton(areLaterPotentialAppointmentsAvailable)
  }

  private fun toggleStateOfDecrementButton(
      selectedAppointmentDate: PotentialAppointmentDate,
      allPotentialAppointmentDates: List<PotentialAppointmentDate>
  ) {
    val areEarlierPotentialAppointmentsAvailable = selectedAppointmentDate > allPotentialAppointmentDates.first()

    ui.enableDecrementButton(areEarlierPotentialAppointmentsAvailable)
  }
}
