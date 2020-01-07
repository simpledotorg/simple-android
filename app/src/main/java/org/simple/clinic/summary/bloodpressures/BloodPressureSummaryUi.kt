package org.simple.clinic.summary.bloodpressures

import org.simple.clinic.bp.BloodPressureMeasurement
import java.util.UUID

interface BloodPressureSummaryUi {
  fun populateBloodPressures(bloodPressureMeasurements: List<BloodPressureMeasurement>)
  fun showBloodPressureEntrySheet(patientUuid: UUID)
  fun showBloodPressureUpdateSheet(bloodPressureMeasurementUuid: UUID)
}
