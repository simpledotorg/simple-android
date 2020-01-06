package org.simple.clinic.summary.bloodpressures

import org.simple.clinic.bp.BloodPressureMeasurement

interface BloodPressureSummaryUi {
  fun populateBloodPressures(bloodPressureMeasurements: List<BloodPressureMeasurement>)
}
