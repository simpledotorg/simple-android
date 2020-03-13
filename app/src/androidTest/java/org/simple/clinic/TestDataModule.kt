package org.simple.clinic

import dagger.Module
import dagger.Provides
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession

@Module
class TestDataModule {

  @Provides
  fun provideTestUser(userSession: UserSession): User {
    return userSession.loggedInUserImmediate()!!
  }
}
