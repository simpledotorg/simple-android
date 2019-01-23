package org.simple.clinic.reports

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class ReportsModule {

  @Provides
  fun reportsApi(retrofit: Retrofit): ReportsApi = retrofit.create(ReportsApi::class.java)
}
