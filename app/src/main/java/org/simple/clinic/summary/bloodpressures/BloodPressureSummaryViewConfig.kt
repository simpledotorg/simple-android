package org.simple.clinic.summary.bloodpressures

import org.simple.clinic.remoteconfig.ConfigReader

data class BloodPressureSummaryViewConfig(
    val numberOfBpsToDisplay: Int,
    val numberOfBpsToDisplayWithoutDiabetesManagement: Int
) {
  companion object {
    fun read(config: ConfigReader): BloodPressureSummaryViewConfig {
      val numberOfBpsToDisplay = config.long("number_of_bps_to_display", 3)
      val numberOfBpsToDisplayWithoutDiabetesManagement = config.long("number_of_bps_to_display_without_diabetes_management", 8)

      return BloodPressureSummaryViewConfig(
          numberOfBpsToDisplay = numberOfBpsToDisplay.toInt(),
          numberOfBpsToDisplayWithoutDiabetesManagement = numberOfBpsToDisplayWithoutDiabetesManagement.toInt()
      )
    }
  }
}
