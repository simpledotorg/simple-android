package org.simple.clinic.summary

import org.simple.clinic.remoteconfig.ConfigReader
import org.threeten.bp.Duration

data class PatientSummaryConfig(
    val bpEditableDuration: Duration,
    val bloodSugarEditableDuration: Duration,
    val numberOfBloodSugarsToDisplay: Int,
    val isDiabetesEnabled: Boolean
) {

  companion object {

    fun read(configReader: ConfigReader): PatientSummaryConfig {
      val bpEditableDurationConfig = configReader.long("bp_editable_duration_in_seconds", 3600)
      val bloodSugarEditableDurationConfig = configReader.long("blood_sugar_editable_duration_in_seconds", 3600)
      val isDiabetesEnabled = configReader.boolean("diabetes_enabled", false)
      val numberOfMeasurementsToDisplay = configReader.long("number_of_measurements_to_display_in_summary", 3)

      return PatientSummaryConfig(
          bpEditableDuration = Duration.ofSeconds(bpEditableDurationConfig),
          bloodSugarEditableDuration = Duration.ofSeconds(bloodSugarEditableDurationConfig),
          numberOfBloodSugarsToDisplay = numberOfMeasurementsToDisplay.toInt(),
          isDiabetesEnabled = isDiabetesEnabled)
    }
  }
}
