package org.simple.clinic.sync.indicator

import java.time.Duration
import java.time.temporal.ChronoUnit

data class SyncIndicatorConfig(val syncFailureThreshold: Duration) {

  companion object {
    fun read() =
        SyncIndicatorConfig(syncFailureThreshold = Duration.of(12, ChronoUnit.HOURS))
  }
}
