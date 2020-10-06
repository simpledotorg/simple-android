package org.simple.clinic.teleconsultlog.shareprescription

import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientProfile
import java.time.LocalDate

interface TeleconsultSharePrescriptionUi {
  fun renderPatientDetails(patient: Patient)
  fun renderPrescriptionDate(prescriptionDate: LocalDate)
  fun renderPatientInformation(patientProfile: PatientProfile)

}
