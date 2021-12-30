package org.simple.clinic.summary.nextappointment

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.overdue.Appointment

class NextAppointmentUiRenderer(private val ui: NextAppointmentUi) : ViewRenderer<NextAppointmentModel> {

  override fun render(model: NextAppointmentModel) {
    if (!model.hasAppointment) {
      renderNoAppointmentView()
    } else {
      renderAppointmentView(model.appointment!!)
    }
  }

  private fun renderAppointmentView(appointment: Appointment) {
    ui.showAppointmentDate(appointment.scheduledDate)
    ui.showChangeAppointmentButton()
  }

  private fun renderNoAppointmentView() {
    ui.showNoAppointment()
    ui.showAddAppointmentButton()
  }
}
