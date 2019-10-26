package org.simple.clinic.setup

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.user.User

@Module
class SetupActivityModule {

  @Provides
  fun provideUserDao(appDatabase: AppDatabase): User.RoomDao = appDatabase.userDao()
}
