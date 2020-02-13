package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.remoteconfig.ConfigReader
import org.threeten.bp.Duration

data class BloodSugarSummaryConfig(
    val bloodSugarEditableDuration: Duration,
    val numberOfBloodSugarsToDisplay: Int,
    val bloodSugarEditFeatureEnabled: Boolean
) {

  companion object {
    fun read(configReader: ConfigReader, bloodSugarEditFeatureEnabled: Boolean): BloodSugarSummaryConfig {
      val bloodSugarEditableDurationSeconds = configReader.long("blood_sugar_editable_duration_in_seconds", 3600)
      val numberOfMeasurementsToDisplay = configReader.long("number_of_measurements_to_display_in_summary", 3)

      return BloodSugarSummaryConfig(
          bloodSugarEditableDuration = Duration.ofSeconds(bloodSugarEditableDurationSeconds),
          numberOfBloodSugarsToDisplay = numberOfMeasurementsToDisplay.toInt(),
          bloodSugarEditFeatureEnabled = bloodSugarEditFeatureEnabled
      )
    }
  }
}
