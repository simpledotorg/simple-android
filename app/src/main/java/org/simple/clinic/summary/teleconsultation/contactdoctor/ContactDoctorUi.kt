package org.simple.clinic.summary.teleconsultation.contactdoctor

import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer

interface ContactDoctorUi {
  fun showMedicalOfficers(medicalOfficers: List<MedicalOfficer>)
}
