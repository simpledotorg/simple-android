package org.simple.clinic.util

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset.UTC

fun Instant.toLocalDateAtZone(zone: ZoneId): LocalDate {
  return this.atZone(UTC)
      .withZoneSameInstant(zone)
      .toLocalDate()
}

fun LocalDate.toUtcInstant(userClock: UserClock): Instant {
  val userTime = LocalTime.now(userClock)
  val userDateTime = this.atTime(userTime)
  val utcDateTime = userDateTime.atZone(userClock.zone).withZoneSameInstant(UTC)
  return utcDateTime.toInstant()
}
