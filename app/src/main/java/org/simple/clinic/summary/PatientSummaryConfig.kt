package org.simple.clinic.summary

import org.simple.clinic.remoteconfig.ConfigReader
import org.threeten.bp.Duration
import javax.inject.Inject

data class PatientSummaryConfig(
    val bpEditableDuration: Duration,
    val callPatientFeatureEnabled: Boolean
) {

  @Inject
  constructor(configReader: ConfigReader) : this(
      bpEditableDuration = Duration.ofSeconds(configReader.long("bp_editable_duration_in_seconds", 3600)),
      callPatientFeatureEnabled = configReader.boolean("call_patient_from_summary_feature_enabled", false)
  )
}
