package org.simple.clinic.overdue.download.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.overdue.download.OverdueListDownloadApi
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Named

@Module
object OverdueListDownloadModule {

  @Provides
  fun providesApi(@Named("for_deployment") retrofit: Retrofit): OverdueListDownloadApi = retrofit.create()
}
