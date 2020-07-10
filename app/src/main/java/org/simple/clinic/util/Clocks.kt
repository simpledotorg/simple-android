package org.simple.clinic.util

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset.UTC

open class UtcClock : Clock() {

  private val utcClock = Clock.systemUTC()

  override fun withZone(zone: ZoneId): Clock = utcClock.withZone(zone)

  override fun getZone(): ZoneId = utcClock.zone

  override fun instant(): Instant = utcClock.instant()
}

abstract class UserClock : Clock()

open class RealUserClock(userTimeZone: ZoneId) : UserClock() {

  private val userClock = Clock.system(userTimeZone)

  override fun withZone(zone: ZoneId): Clock = userClock.withZone(zone)

  override fun getZone(): ZoneId = userClock.zone

  override fun instant(): Instant = userClock.instant()
}

open class ElapsedRealtimeClock(private val zone: ZoneId = UTC) : Clock() {

  override fun withZone(zone: ZoneId): Clock = ElapsedRealtimeClock(zone)

  override fun getZone(): ZoneId = zone

  override fun instant(): Instant = Instant.ofEpochMilli(android.os.SystemClock.elapsedRealtime())
}
