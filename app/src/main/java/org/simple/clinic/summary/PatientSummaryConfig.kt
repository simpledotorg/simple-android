package org.simple.clinic.summary

import org.simple.clinic.remoteconfig.ConfigReader
import org.threeten.bp.Duration

data class PatientSummaryConfig(
    val bpEditableDuration: Duration,
    val callPatientFeatureEnabled: Boolean
) {

  companion object {

    fun read(configReader: ConfigReader): PatientSummaryConfig {
      val bpEditableDurationConfig = configReader.long("bp_editable_duration_in_seconds", 3600)

      return PatientSummaryConfig(
          bpEditableDuration = Duration.ofSeconds(bpEditableDurationConfig),
          callPatientFeatureEnabled = configReader.boolean("call_patient_from_summary_feature_enabled", false)
      )
    }
  }
}
