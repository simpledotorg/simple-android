package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import java.util.UUID

data class BloodSugarSummaryViewModel(
    val patientUuid: UUID,
    val measurements: List<BloodSugarMeasurement>?
) {

  companion object {
    fun create(patientUuid: UUID): BloodSugarSummaryViewModel {
      return BloodSugarSummaryViewModel(patientUuid, null)
    }
  }

  fun summaryFetched(measurements: List<BloodSugarMeasurement>): BloodSugarSummaryViewModel {
    return copy(measurements = measurements)
  }
}
