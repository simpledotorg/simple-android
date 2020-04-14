package org.simple.clinic.contactpatient

import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.util.ParcelableOptional
import org.simple.clinic.util.UserClock

class ContactPatientUiRenderer(
    private val ui: ContactPatientUi,
    private val clock: UserClock
) : ViewRenderer<ContactPatientModel> {

  override fun render(model: ContactPatientModel) {
    if (model.hasLoadedPatientProfile) {
      renderPatientProfile(model.patientProfile!!)
    }

    if (model.hasLoadedAppointment) {
      toggleCallResultSection(model.appointment!!)
    }

    if (model.secureCallingFeatureEnabled) {
      ui.showSecureCallUi()
    } else {
      ui.hideSecureCallUi()
    }
  }

  private fun toggleCallResultSection(appointment: ParcelableOptional<OverdueAppointment>) {
    if (appointment.isEmpty()) {
      ui.hideCallResultSection()
    } else {
      ui.showCallResultSection()
    }
  }

  private fun renderPatientProfile(patientProfile: PatientProfile) {
    val patientAge = DateOfBirth.fromPatient(patientProfile.patient, clock).estimateAge(clock)

    ui.renderPatientDetails(
        name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientAge,
        phoneNumber = patientProfile.phoneNumbers.first().number
    )
  }
}
