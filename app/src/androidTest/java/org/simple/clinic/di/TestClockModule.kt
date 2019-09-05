package org.simple.clinic.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.util.ElapsedRealtimeClock
import org.simple.clinic.util.TestElapsedRealtimeClock
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock

@Module
class TestClockModule {

  @Provides
  fun testUtcClock(utcClock: UtcClock): TestUtcClock {
    return utcClock as TestUtcClock
  }

  @Provides
  fun testUserClock(userClock: UserClock): TestUserClock {
    return userClock as TestUserClock
  }

  @Provides
  fun testElapsedRealtimeClock(elapsedRealtimeClock: ElapsedRealtimeClock): TestElapsedRealtimeClock {
    return elapsedRealtimeClock as TestElapsedRealtimeClock
  }
}
