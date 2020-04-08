package org.simple.clinic.patientcontact

import org.simple.clinic.patient.Gender

interface PatientContactUi {
  fun renderPatientDetails(name: String, gender: Gender, age: Int, phoneNumber: String)
}
