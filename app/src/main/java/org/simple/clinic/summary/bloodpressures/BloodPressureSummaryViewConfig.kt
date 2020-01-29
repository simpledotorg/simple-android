package org.simple.clinic.summary.bloodpressures

import org.simple.clinic.remoteconfig.ConfigReader

data class BloodPressureSummaryViewConfig(val numberOfBpsToDisplay: Int) {
  companion object {
    fun read(config: ConfigReader): BloodPressureSummaryViewConfig {
      val numberOfBpsToDisplay = config.long("number_of_bps_to_display", 3)

      return BloodPressureSummaryViewConfig(
          numberOfBpsToDisplay = numberOfBpsToDisplay.toInt()
      )
    }
  }
}
