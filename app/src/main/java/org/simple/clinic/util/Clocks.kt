package org.simple.clinic.util

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

open class UtcClock : Clock() {

  private val utcClock = Clock.systemUTC()

  override fun withZone(zone: ZoneId): Clock = utcClock.withZone(zone)

  override fun getZone(): ZoneId = utcClock.zone

  override fun instant(): Instant = utcClock.instant()
}

abstract class UserClock: Clock()

open class RealUserClock(userTimeZone: ZoneId) : UserClock() {

  private val userClock = Clock.system(userTimeZone)

  override fun withZone(zone: ZoneId): Clock = userClock.withZone(zone)

  override fun getZone(): ZoneId = userClock.zone

  override fun instant(): Instant = userClock.instant()
}
