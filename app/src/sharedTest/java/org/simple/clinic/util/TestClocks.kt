package org.simple.clinic.util

import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset.UTC

class TestUtcClock : UtcClock() {

  private var clock = fixed(Instant.EPOCH, UTC)

  override fun withZone(zone: ZoneId): Clock = clock.withZone(zone)

  override fun getZone(): ZoneId = clock.zone

  override fun instant(): Instant = clock.instant()

  fun setYear(year: Int) {
    clock = clockFromYear(year)
  }

  fun advanceBy(duration: Duration) {
    clock = Clock.offset(clock, duration)
  }

  fun resetToEpoch() {
    clock = fixed(Instant.EPOCH, UTC)
  }
}

class TestUserClock : UserClock() {

  private var clock = fixed(Instant.EPOCH, UTC)

  override fun withZone(zone: ZoneId): Clock = clock.withZone(zone)

  override fun getZone(): ZoneId = clock.zone

  override fun instant(): Instant = clock.instant()

  fun setYear(year: Int) {
    clock = clockFromYear(year)
  }

  fun advanceBy(duration: Duration) {
    clock = Clock.offset(clock, duration)
  }

  fun resetToEpoch() {
    clock = fixed(Instant.EPOCH, UTC)
  }
}

private fun clockFromYear(year: Int): Clock {
  if (year < 1970) {
    throw AssertionError("year should be >= 1970!")
  }
  val numberOfYearsToAdvanceFromEpoch = (year - 1970).toLong()

  val fixedInstant = LocalDateTime
      .ofInstant(Instant.EPOCH, UTC)
      .plusYears(numberOfYearsToAdvanceFromEpoch)
      .toInstant(UTC)

  return Clock.fixed(fixedInstant, UTC)
}
