package org.simple.clinic.patientcontact

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.util.UserClock

class PatientContactUiRenderer(
    private val ui: PatientContactUi,
    private val clock: UserClock
) : ViewRenderer<PatientContactModel> {

  override fun render(model: PatientContactModel) {
    if (model.hasLoadedPatientProfile) {
      renderPatientProfile(model.patientProfile!!)
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
