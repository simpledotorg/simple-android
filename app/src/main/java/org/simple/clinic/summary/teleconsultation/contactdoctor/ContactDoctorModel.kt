package org.simple.clinic.summary.teleconsultation.contactdoctor

import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer

data class ContactDoctorModel(
    val medicalOfficers: List<MedicalOfficer>?
) {

  companion object {
    fun create() = ContactDoctorModel(
        medicalOfficers = null
    )
  }

  fun medicalOfficersLoaded(medicalOfficers: List<MedicalOfficer>): ContactDoctorModel {
    return copy(medicalOfficers = medicalOfficers)
  }
}
