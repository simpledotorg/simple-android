package org.simple.clinic.di

import android.app.Application
import org.simple.clinic.util.ElapsedRealtimeClock
import org.simple.clinic.util.TestElapsedRealtimeClock
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.threeten.bp.ZoneId

class TestAppModule(application: Application) : AppModule(application) {

  override fun utcClock(): UtcClock = TestUtcClock()

  override fun userClock(userTimeZone: ZoneId): UserClock = TestUserClock()

  override fun elapsedRealtimeClock(): ElapsedRealtimeClock = TestElapsedRealtimeClock()
}
