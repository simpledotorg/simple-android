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

  @AppScope
  @Provides
  fun testUtcClock(): TestUtcClock {
    return TestUtcClock()
  }

  @AppScope
  @Provides
  fun testUserClock(): TestUserClock {
    return TestUserClock()
  }

  @AppScope
  @Provides
  fun testElapsedRealtimeClock(): TestElapsedRealtimeClock {
    return TestElapsedRealtimeClock()
  }

  @Provides
  fun utcClock(testUtcClock: TestUtcClock): UtcClock = testUtcClock

  @Provides
  fun userClock(testUserClock: TestUserClock): UserClock = testUserClock

  @Provides
  fun elapsedRealtimeClock(): ElapsedRealtimeClock = TestElapsedRealtimeClock()
}
