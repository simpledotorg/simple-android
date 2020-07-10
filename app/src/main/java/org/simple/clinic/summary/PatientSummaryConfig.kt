package org.simple.clinic.summary

import org.simple.clinic.remoteconfig.ConfigReader
import java.time.Duration
import javax.inject.Inject

data class PatientSummaryConfig(
    val bpEditableDuration: Duration,
    val numberOfMeasurementsForTeleconsultation: Int
) {

  @Inject
  constructor(configReader: ConfigReader) : this(
      bpEditableDuration = Duration.ofSeconds(configReader.long("bp_editable_duration_in_seconds", 3600)),
      numberOfMeasurementsForTeleconsultation = configReader.long("number_of_measurements_to_show_in_whatsapp_message", 3).toInt()
  )
}
