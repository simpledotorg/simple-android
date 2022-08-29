package org.simple.clinic.summary.nextappointment

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.daysTill
import kotlin.math.absoluteValue

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

    when {
      appointmentDate == model.currentDate -> {
        ui.showAppointmentDate(appointmentDate)
      }
      appointmentDate > model.currentDate -> {
        ui.showAppointmentDateWithRemainingDays(appointmentDate, appointmentDate.daysTill(model.currentDate).absoluteValue)
      }
      appointmentDate < model.currentDate -> {
        ui.showAppointmentDateWithOverdueDays(appointmentDate, appointmentDate.daysTill(model.currentDate).absoluteValue)
      }
    }
  }

  private fun renderNoAppointmentView() {
    ui.showNoAppointment()
    ui.showAddAppointmentButton()
    ui.hideAppointmentFacility()
  }
}
