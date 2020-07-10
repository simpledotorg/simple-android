package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.remoteconfig.ConfigReader
import java.time.Duration

data class BloodSugarSummaryConfig(
    val bloodSugarEditableDuration: Duration,
    val numberOfBloodSugarsToDisplay: Int
) {

  companion object {
    fun read(configReader: ConfigReader): BloodSugarSummaryConfig {
      val bloodSugarEditableDurationSeconds = configReader.long("blood_sugar_editable_duration_in_seconds", 3600)
      val numberOfMeasurementsToDisplay = configReader.long("number_of_measurements_to_display_in_summary", 3)

      return BloodSugarSummaryConfig(
          bloodSugarEditableDuration = Duration.ofSeconds(bloodSugarEditableDurationSeconds),
          numberOfBloodSugarsToDisplay = numberOfMeasurementsToDisplay.toInt()
      )
    }
  }
}
