package org.simple.clinic.summary

import io.reactivex.Observable
import org.simple.clinic.remoteconfig.ConfigReader
import org.threeten.bp.Duration

data class PatientSummaryConfig(
    val numberOfBpPlaceholders: Int,
    val numberOfBpsToDisplay: Int,
    val bpEditableDuration: Duration
) {

  companion object {

    fun read(configReader: ConfigReader): Observable<PatientSummaryConfig> {
      return Observable.fromCallable {
        val bpEditableDurationConfig = configReader.long("bp_editable_duration_in_seconds", 3600)

        PatientSummaryConfig(
            numberOfBpPlaceholders = 3,
            numberOfBpsToDisplay = 100,
            bpEditableDuration = Duration.ofSeconds(bpEditableDurationConfig))
      }
    }
  }
}
