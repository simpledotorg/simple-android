package org.simple.clinic.teleconsultlog.prescription.patientinfo

import org.simple.clinic.patient.PatientProfile
import java.time.LocalDate

interface TeleconsultPatientInfoUi {
  fun renderPatientInformation(patientProfile: PatientProfile)
  fun renderPrescriptionDate(prescriptionDate: LocalDate)
}
