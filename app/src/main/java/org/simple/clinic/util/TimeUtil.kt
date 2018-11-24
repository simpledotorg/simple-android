package org.simple.clinic.util

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Period
import org.threeten.bp.ZoneOffset

fun estimateCurrentAge(recordedAge: Int, ageRecordedAtTimestamp: Instant, clock: Clock): Int {
  val ageRecordedAtTime = LocalDateTime.ofInstant(ageRecordedAtTimestamp, ZoneOffset.UTC)
      .let { LocalDate.of(it.year, it.month, it.dayOfMonth) }
  val nowTime = LocalDate.now(clock)

  val yearsPassedSinceAgeRecorded = Period.between(ageRecordedAtTime, nowTime).years
  return yearsPassedSinceAgeRecorded + recordedAge
}
