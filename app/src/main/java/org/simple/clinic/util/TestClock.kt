package org.simple.clinic.util

import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset.UTC

class TestClock : Clock() {

  private var clock = fixed(Instant.EPOCH, UTC)

  override fun withZone(zoneId: ZoneId?): Clock = clock.withZone(zoneId)

  override fun getZone(): ZoneId = clock.zone

  override fun instant(): Instant = clock.instant()

  fun setYear(year: Int) {
    if (year < 1970) {
      throw AssertionError("year should be >= 1970!")
    }
    val numberOfYearsToAdvanceFromEpoch = (year - 1970).toLong()

    val fixedInstant = LocalDateTime
        .ofInstant(Instant.EPOCH, UTC)
        .plusYears(numberOfYearsToAdvanceFromEpoch)
        .toInstant(UTC)

    clock = fixed(fixedInstant, UTC)
  }

  fun advanceBy(duration: Duration) {
    clock = Clock.offset(clock, duration)
  }

  fun resetToEpoch() {
    clock = fixed(Instant.EPOCH, UTC)
  }
}
