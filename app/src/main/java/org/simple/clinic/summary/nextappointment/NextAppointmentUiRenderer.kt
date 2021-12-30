package org.simple.clinic.summary.nextappointment

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.PatientAndAssignedFacility

class NextAppointmentUiRenderer(private val ui: NextAppointmentUi) : ViewRenderer<NextAppointmentModel> {

  override fun render(model: NextAppointmentModel) {
    if (!model.hasAppointment && !model.hasPatientAndAssignedFacility) {
      renderNoAppointmentView()
    } else {
      renderAppointmentView(model.appointment!!, model.patientAndAssignedFacility!!)
    }
  }

  private fun renderAppointmentView(appointment: Appointment, patientAndAssignedFacility: PatientAndAssignedFacility) {
    ui.showAppointmentDate(appointment.scheduledDate)
    ui.showChangeAppointmentButton()

    if (patientAndAssignedFacility.hasAssignedFacility && patientAndAssignedFacility.assignedFacilityId != appointment.facilityUuid) {
      ui.showAssignedFacility(patientAndAssignedFacility.assignedFacility!!.name)
    } else {
      ui.hideAssignedFacility()
    }
  }

  private fun renderNoAppointmentView() {
    ui.showNoAppointment()
    ui.showAddAppointmentButton()
    ui.hideAssignedFacility()
  }
}
