package org.simple.clinic.summary.nextappointment

import org.simple.clinic.mobius.ViewRenderer

class NextAppointmentUiRenderer(private val ui: NextAppointmentUi) : ViewRenderer<NextAppointmentModel> {

  override fun render(model: NextAppointmentModel) {
    if (!model.hasNextAppointmentPatientProfile) {
      renderNoAppointmentView()
    } else {
      renderAppointmentView(model)
    }
  }

  private fun renderAppointmentView(model: NextAppointmentModel) {
    ui.showAppointmentDate(model.appointment.scheduledDate)
    ui.showChangeAppointmentButton()

    if (!model.appointmentIsInAssignedFacility) {
      ui.showAppointmentFacility(model.appointmentFacilityName)
    } else {
      ui.hideAppointmentFacility()
    }
  }

  private fun renderNoAppointmentView() {
    ui.showNoAppointment()
    ui.showAddAppointmentButton()
    ui.hideAppointmentFacility()
  }
}
