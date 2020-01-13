package org.simple.clinic.bp.history

import org.simple.clinic.bp.BloodPressureMeasurement

interface BloodPressureHistoryScreenUi {
  fun showBloodPressureHistory(bloodPressures: List<BloodPressureMeasurement>)
}
