package org.simple.clinic.scheduleappointment

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.util.ValueChangedCallback

class ScheduleAppointmentUiRenderer(
    private val ui: ScheduleAppointmentUi
) : ViewRenderer<ScheduleAppointmentModel> {

  private val selectedDateChangedCallback = ValueChangedCallback<PotentialAppointmentDate>()

  override fun render(model: ScheduleAppointmentModel) {
    if (model.hasLoadedAppointmentDate) {
      selectedDateChangedCallback.pass(model.selectedAppointmentDate!!) { selectedDate ->
        ui.updateScheduledAppointment(selectedDate.scheduledFor, selectedDate.timeToAppointment)
        toggleStateOfIncrementButton(selectedDate, model.potentialAppointmentDates)
        toggleStateOfDecrementButton(selectedDate, model.potentialAppointmentDates)
      }
    }
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
