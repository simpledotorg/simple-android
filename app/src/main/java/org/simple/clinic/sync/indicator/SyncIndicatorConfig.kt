package org.simple.clinic.sync.indicator

import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

data class SyncIndicatorConfig(val syncFailureThreshold: Duration) {

  companion object {
    fun read() =
        SyncIndicatorConfig(syncFailureThreshold = Duration.of(12, ChronoUnit.HOURS))
  }
}
