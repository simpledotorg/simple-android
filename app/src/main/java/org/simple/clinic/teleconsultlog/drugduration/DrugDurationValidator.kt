package org.simple.clinic.teleconsultlog.drugduration

import javax.inject.Inject

class DrugDurationValidator @Inject constructor(
    private val drugDurationConfig: DrugDurationConfig
) {

  fun validate(drugDuration: String): DrugDurationValidationResult {
    return when {
      drugDuration.isBlank() -> Blank
      drugDuration.toInt() > drugDurationConfig.maxAllowedDuration.toDays() -> MaxDrugDuration(drugDurationConfig.maxAllowedDuration.toDays().toInt())
      else -> Valid
    }
  }
}
