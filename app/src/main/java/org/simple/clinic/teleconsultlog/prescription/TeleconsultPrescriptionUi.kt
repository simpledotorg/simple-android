package org.simple.clinic.teleconsultlog.prescription

import org.simple.clinic.patient.Patient

interface TeleconsultPrescriptionUi {
  fun renderPatientDetails(patient: Patient)
}
