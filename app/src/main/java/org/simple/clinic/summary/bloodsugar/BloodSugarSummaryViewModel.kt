package org.simple.clinic.summary.bloodsugar

import java.util.UUID

data class BloodSugarSummaryViewModel(val patientUuid: UUID) {

  companion object {
    fun create(patientUuid: UUID): BloodSugarSummaryViewModel {
      return BloodSugarSummaryViewModel(patientUuid)
    }
  }

  fun summaryFetched(): BloodSugarSummaryViewModel {
    return create(patientUuid)
  }
}
