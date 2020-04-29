package org.simple.clinic.reports

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
class ReportsModule {
  @Provides
  fun reportsApi(@Named("for_country") retrofit: Retrofit): ReportsApi = retrofit.create(ReportsApi::class.java)

  @Provides
  @Named("reports_file_path")
  fun reportsFilePath() = "report.html"
}
