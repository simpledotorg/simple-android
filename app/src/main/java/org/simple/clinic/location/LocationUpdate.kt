package org.simple.clinic.location

import org.simple.clinic.util.ElapsedRealtimeClock
import java.time.Duration

sealed class LocationUpdate {

  object Unavailable : LocationUpdate()

  data class Available(
      val location: Coordinates,
      val timeSinceBootWhenRecorded: Duration
  ) : LocationUpdate() {

    fun age(elapsedRealtimeClock: ElapsedRealtimeClock): Duration {
      val timeSinceSystemBoot = Duration.ofMillis(elapsedRealtimeClock.instant().toEpochMilli())
      return timeSinceSystemBoot - timeSinceBootWhenRecorded
    }
  }

  fun isRecent(
      clock: ElapsedRealtimeClock,
      locationStaleAfter: Duration
  ): Boolean {
    return when(this) {
      Unavailable -> true
      is Available -> age(clock) <= locationStaleAfter
    }
  }
}
