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
    renderAppointmentDate(model)
    ui.showChangeAppointmentButton()

    if (!model.appointmentIsInAssignedFacility) {
      ui.showAppointmentFacility(model.appointmentFacilityName)
    } else {
      ui.hideAppointmentFacility()
    }
  }

  private fun renderAppointmentDate(model: NextAppointmentModel) {
    val appointmentDate = model.appointment.scheduledDate

    if (appointmentDate == model.currentDate) {
      ui.showAppointmentDate(appointmentDate)
    }
  }

  private fun renderNoAppointmentView() {
    ui.showNoAppointment()
    ui.showAddAppointmentButton()
    ui.hideAppointmentFacility()
  }
}
