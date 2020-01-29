package org.simple.clinic.summary.bloodpressures

import org.simple.clinic.remoteconfig.ConfigReader

data class NewBloodPressureSummaryViewConfig(val numberOfBpsToDisplay: Int) {
  companion object {
    fun read(config: ConfigReader): NewBloodPressureSummaryViewConfig {
      val numberOfBpsToDisplay = config.long("number_of_bps_to_display", 3)

      return NewBloodPressureSummaryViewConfig(
          numberOfBpsToDisplay = numberOfBpsToDisplay.toInt()
      )
    }
  }
}
