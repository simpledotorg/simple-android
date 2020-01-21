package org.simple.clinic.summary.bloodpressures.newbpsummary

import java.util.UUID

data class NewBloodPressureSummaryViewModel(
    val patientUuid: UUID
) {
  companion object {
    fun create(patientUuid: UUID) = NewBloodPressureSummaryViewModel(patientUuid)
  }
}
