package org.simple.clinic.util

import org.threeten.bp.Clock
import org.threeten.bp.ZoneId

open class UtcClock : Clock() {

  private val utcClock = Clock.systemUTC()

  override fun withZone(zone: ZoneId) = utcClock.withZone(zone)!!

  override fun getZone() = utcClock.zone!!

  override fun instant() = utcClock.instant()!!
}

open class UserClock(userTimeZone: ZoneId) : Clock() {

  private val userClock = Clock.system(userTimeZone)

  override fun withZone(zone: ZoneId) = userClock.withZone(zone)!!

  override fun getZone() = userClock.zone!!

  override fun instant() = userClock.instant()!!
}
