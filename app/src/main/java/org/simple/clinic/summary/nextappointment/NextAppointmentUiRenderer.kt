package org.simple.clinic.summary.nextappointment

import org.simple.clinic.mobius.ViewRenderer

class NextAppointmentUiRenderer(private val ui: NextAppointmentUi) : ViewRenderer<NextAppointmentModel> {

  override fun render(model: NextAppointmentModel) {
    if (!model.hasNextAppointmentPatientProfile) {
      renderNoAppointmentView()
    }
  }

  private fun renderNoAppointmentView() {
    ui.showNoAppointment()
    ui.showAddAppointmentButton()
  }
}
