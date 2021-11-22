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

  override fun begin(operation: SqlPerformanceReporter.SqlOperation) {
    // Nothing to do here for reporting to Mixpanel
  }

  override fun end(operation: SqlPerformanceReporter.SqlOperation) {
    if (sampler.sample) {
      val timeTaken = Duration.ofMillis(System.currentTimeMillis() - operation.startTimeMillis)
      Analytics.reportSqlOperation(operation.daoName, operation.methodName, timeTaken)
    }
  }
}
