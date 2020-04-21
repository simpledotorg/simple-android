package org.simple.clinic.summary

import org.simple.clinic.remoteconfig.ConfigReader
import org.threeten.bp.Duration
import javax.inject.Inject

data class PatientSummaryConfig(
    val bpEditableDuration: Duration
) {

  @Inject
  constructor(configReader: ConfigReader) : this(
      bpEditableDuration = Duration.ofSeconds(configReader.long("bp_editable_duration_in_seconds", 3600))
  )
}
