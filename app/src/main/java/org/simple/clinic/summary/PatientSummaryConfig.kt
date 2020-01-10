package org.simple.clinic.summary

import org.simple.clinic.remoteconfig.ConfigReader
import org.threeten.bp.Duration

data class PatientSummaryConfig(
    val numberOfBpPlaceholders: Int,
    val numberOfBpsToDisplay: Int,
    val bpEditableDuration: Duration,
    val numberOfBloodSugarsToDisplay: Int,
    val isDiabetesEnabled: Boolean
) {

  companion object {

    fun read(configReader: ConfigReader): PatientSummaryConfig {
      val bpEditableDurationConfig = configReader.long("bp_editable_duration_in_seconds", 3600)
      val isDiabetesEnabled = configReader.boolean("diabetes_enabled", false)

      return PatientSummaryConfig(
          numberOfBpPlaceholders = 3,
          numberOfBpsToDisplay = 100,
          bpEditableDuration = Duration.ofSeconds(bpEditableDurationConfig),
          numberOfBloodSugarsToDisplay = 100,
          isDiabetesEnabled = isDiabetesEnabled)
    }
  }
}
