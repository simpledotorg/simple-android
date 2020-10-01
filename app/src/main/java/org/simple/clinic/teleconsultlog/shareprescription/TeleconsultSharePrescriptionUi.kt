package org.simple.clinic.teleconsultlog.shareprescription

import org.simple.clinic.patient.Patient
import java.time.LocalDate

interface TeleconsultSharePrescriptionUi {
  fun renderPatientDetails(patient: Patient)
  fun renderPrescriptionDate(prescriptionDate: LocalDate)
}
