package org.simple.clinic.storage.monitoring

import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.remoteconfig.ConfigReader
import java.time.Duration
import javax.inject.Inject

class AnalyticsSqlPerformanceReportingSink(
    private val sampler: Sampler
) : SqlPerformanceReporter.ReportSink {

  @Inject
  constructor(
      remoteConfig: ConfigReader
  ) : this(
      sampler = Sampler(remoteConfig.double("room_query_profile_sample_rate", 0.0).toFloat())
  )

  override fun report(daoName: String, methodName: String, timeTaken: Duration) {
    if (sampler.sample) {
      Analytics.reportSqlOperation(daoName, methodName, timeTaken)
    }
  }
}
