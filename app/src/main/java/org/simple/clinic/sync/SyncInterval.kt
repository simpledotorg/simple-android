package org.simple.clinic.sync

import java.time.Duration

enum class SyncInterval(val frequency: Duration, val backOffDelay: Duration) {
  FREQUENT(frequency = Duration.ofMinutes(16L), backOffDelay = Duration.ofMinutes(5L)),
  DAILY(frequency = Duration.ofDays(1L), backOffDelay = Duration.ofMinutes(5L));

  companion object {
    fun mostFrequent(): Duration {
      return enumValues<SyncInterval>()
          .map { it.frequency }
          .minOrNull()!!
    }
  }
}
