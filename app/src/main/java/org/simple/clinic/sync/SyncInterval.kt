package org.simple.clinic.sync

import java.time.Duration

enum class SyncInterval(val frequency: Duration, val backOffDelay: Duration) {
  /*
  * If you decide to rename these values, keep in mind that the names of
  * these enums are persisted by WorkManager in org.simple.clinic.sync.SyncScheduler
  * to uniquely identify periodic syncs. You might need to write some
  * additional code to handle this scenario.
  **/
  FREQUENT(frequency = Duration.ofMinutes(16L), backOffDelay = Duration.ofMinutes(5L)),
  DAILY(frequency = Duration.ofDays(1L), backOffDelay = Duration.ofMinutes(5L));

  companion object {
    fun mostFrequent(): Duration {
      return enumValues<SyncInterval>()
          .map { it.frequency }
          .min()!!
    }
  }
}
