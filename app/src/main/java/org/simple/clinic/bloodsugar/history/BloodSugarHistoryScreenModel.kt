package org.simple.clinic.bloodsugar.history

import java.util.UUID

data class BloodSugarHistoryScreenModel(val patientUuid: UUID) {
  companion object {
    fun create(patientUuid: UUID) = BloodSugarHistoryScreenModel(patientUuid)
  }
}
