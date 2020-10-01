package org.simple.clinic.teleconsultlog.shareprescription

import org.simple.clinic.patient.Patient

interface TeleconsultSharePrescriptionUi {
  fun renderPatientDetails(patient: Patient)
}
