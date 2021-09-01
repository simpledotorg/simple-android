package org.simple.clinic.overdue.callresult.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.overdue.callresult.CallResultSyncApi
import retrofit2.Retrofit
import javax.inject.Named

@Module
class CallResultModule {

  @Provides
  fun provideRoomDao(appDatabase: AppDatabase) = appDatabase.callResultDao()

  @Provides
  fun provideApi(@Named("for_deployment") retrofit: Retrofit): CallResultSyncApi {
    return retrofit.create(CallResultSyncApi::class.java)
  }
}
