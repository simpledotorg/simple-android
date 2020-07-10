package org.simple.clinic.util

import org.simple.clinic.overdue.TimeToAppointment
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit

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

fun LocalDate.plus(timeToAppointment: TimeToAppointment): LocalDate {
  return this.plus(
      timeToAppointment.value.toLong(),
      when (timeToAppointment) {
        is TimeToAppointment.Days -> ChronoUnit.DAYS
        is TimeToAppointment.Weeks -> ChronoUnit.WEEKS
        is TimeToAppointment.Months -> ChronoUnit.MONTHS
      }
  )
}

infix fun LocalDate.daysTill(other: LocalDate): Int = ChronoUnit.DAYS.between(this, other).toInt()
