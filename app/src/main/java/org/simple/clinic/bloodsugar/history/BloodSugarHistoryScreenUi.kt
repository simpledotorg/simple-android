package org.simple.clinic.bloodsugar.history

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.patient.Patient

interface BloodSugarHistoryScreenUi {
  fun showPatientInformation(patient: Patient)
  fun showBloodSugarHistory(bloodSugars: List<BloodSugarMeasurement>)
}
