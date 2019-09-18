package org.simple.clinic.util

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.Period
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset.UTC

fun estimateCurrentAge(recordedAge: Int, ageRecordedAtTimestamp: Instant, clock: Clock): Int {
  val ageRecordedAtDate = ageRecordedAtTimestamp.atZone(UTC).toLocalDate()
  val today = LocalDate.now(clock)

  val yearsPassedSinceAgeRecorded = Period.between(ageRecordedAtDate, today).years
  return yearsPassedSinceAgeRecorded + recordedAge
}

fun estimateCurrentAge(recordedAge: Int, ageRecordedAtTimestamp: Instant, clock: UserClock): Int {
  val ageRecordedAtDate = ageRecordedAtTimestamp.toLocalDateAtZone(clock.zone)
  val today = LocalDate.now(clock)

  val yearsPassedSinceAgeRecorded = Period.between(ageRecordedAtDate, today).years
  return recordedAge + yearsPassedSinceAgeRecorded
}

fun estimateCurrentAge(recordedDateOfBirth: LocalDate, clock: Clock): Int {
  return Period.between(recordedDateOfBirth, LocalDate.now(clock)).years
}

fun estimateCurrentAge(recordedDateOfBirth: LocalDate, clock: UserClock): Int {
  return Period.between(recordedDateOfBirth, LocalDate.now(clock)).years
}

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
