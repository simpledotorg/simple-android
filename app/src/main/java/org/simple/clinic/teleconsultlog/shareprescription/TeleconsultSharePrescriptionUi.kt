package org.simple.clinic.teleconsultlog.shareprescription

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.patient.PatientProfile
import java.time.LocalDate

interface TeleconsultSharePrescriptionUi {
  fun renderPrescriptionDate(prescriptionDate: LocalDate)
  fun renderPatientInformation(patientProfile: PatientProfile)
  fun renderPatientMedicines(medicines: List<PrescribedDrug>)
  fun showDownloadProgress()
  fun hideDownloadProgress()
  fun showShareProgress()
  fun hideShareProgress()
}
