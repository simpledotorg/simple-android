package org.simple.clinic.bp.history

import org.simple.clinic.bp.BloodPressureMeasurement
import java.util.UUID

data class BloodPressureHistoryScreenModel(
    val patientUuid: UUID,
    val bloodPressures: List<BloodPressureMeasurement>?
) {

  companion object {
    fun create(patientUuid: UUID) = BloodPressureHistoryScreenModel(patientUuid, null)
  }

  fun historyLoaded(bloodPressures: List<BloodPressureMeasurement>): BloodPressureHistoryScreenModel =
      copy(bloodPressures = bloodPressures)
}
