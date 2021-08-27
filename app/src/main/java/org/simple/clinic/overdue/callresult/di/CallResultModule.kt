package org.simple.clinic.overdue.callresult.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase

@Module
class CallResultModule {

  @Provides
  fun provideRoomDao(appDatabase: AppDatabase) = appDatabase.callResultDao()
}
