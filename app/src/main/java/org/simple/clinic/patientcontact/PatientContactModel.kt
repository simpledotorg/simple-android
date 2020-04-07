package org.simple.clinic.patientcontact

import org.simple.clinic.patient.PatientProfile

data class PatientContactModel(
    private val patientProfile: PatientProfile? = null
) {


  companion object {
    fun create(): PatientContactModel = PatientContactModel()
  }

  fun patientProfileLoaded(patientProfile: PatientProfile): PatientContactModel {
    return copy(patientProfile = patientProfile)
  }
}
