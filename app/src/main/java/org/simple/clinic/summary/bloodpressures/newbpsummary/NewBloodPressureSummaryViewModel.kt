package org.simple.clinic.summary.bloodpressures.newbpsummary

import org.simple.clinic.bp.BloodPressureMeasurement
import java.util.UUID

data class NewBloodPressureSummaryViewModel(
    val patientUuid: UUID,
    val latestBloodPressuresToDisplay: List<BloodPressureMeasurement>?
) {
  companion object {
    fun create(patientUuid: UUID) = NewBloodPressureSummaryViewModel(patientUuid, null)
  }

  fun bloodPressuresLoaded(bloodPressures: List<BloodPressureMeasurement>): NewBloodPressureSummaryViewModel =
      copy(latestBloodPressuresToDisplay = bloodPressures)
}
