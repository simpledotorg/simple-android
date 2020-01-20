package org.simple.clinic.bp.history

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.patient.Patient

interface BloodPressureHistoryScreenUi {
  fun showBloodPressureHistory(bloodPressures: List<BloodPressureMeasurement>)
  fun showPatientInformation(patient: Patient)
}
