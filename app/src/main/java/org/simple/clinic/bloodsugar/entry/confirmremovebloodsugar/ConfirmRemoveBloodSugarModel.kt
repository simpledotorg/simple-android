package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import java.util.UUID

data class ConfirmRemoveBloodSugarModel(val bloodSugarMeasurementUuid: UUID) {
  companion object {
    fun create(bloodSugarMeasurementUuid: UUID) = ConfirmRemoveBloodSugarModel(bloodSugarMeasurementUuid)
  }
}
