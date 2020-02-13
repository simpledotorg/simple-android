package org.simple.clinic.summary

import org.simple.clinic.remoteconfig.ConfigReader
import org.threeten.bp.Duration

data class PatientSummaryConfig(
    val bpEditableDuration: Duration,
    val isDiabetesEnabled: Boolean
) {

  companion object {

    fun read(configReader: ConfigReader): PatientSummaryConfig {
      val bpEditableDurationConfig = configReader.long("bp_editable_duration_in_seconds", 3600)
      val isDiabetesEnabled = configReader.boolean("diabetes_enabled", false)

      return PatientSummaryConfig(
          bpEditableDuration = Duration.ofSeconds(bpEditableDurationConfig),
          isDiabetesEnabled = isDiabetesEnabled)
    }
  }
}
