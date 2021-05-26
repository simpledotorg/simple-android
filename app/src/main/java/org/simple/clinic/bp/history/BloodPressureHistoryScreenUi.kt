package org.simple.clinic.bp.history

import org.simple.clinic.patient.Patient

interface BloodPressureHistoryScreenUi {
  fun showPatientInformation(patient: Patient)
}
