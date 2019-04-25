package org.simple.clinic.util

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset.UTC

fun estimateCurrentAge(recordedAge: Int, ageRecordedAtTimestamp: Instant, clock: Clock): Int {
  val ageRecordedAtDate = ageRecordedAtTimestamp.atZone(UTC).toLocalDate()
  val today = LocalDate.now(clock)

  val yearsPassedSinceAgeRecorded = Period.between(ageRecordedAtDate, today).years
  return yearsPassedSinceAgeRecorded + recordedAge
}

fun estimateCurrentAge(recordedDateOfBirth: LocalDate, clock: Clock): Int {
  return Period.between(recordedDateOfBirth, LocalDate.now(clock)).years
}

fun Instant.toLocalDateAtZone(zone: ZoneId): LocalDate =
    atZone(UTC)
        .withZoneSameInstant(zone)
        .toLocalDate()
