package org.simple.clinic.storage.monitoring

import timber.log.Timber
import java.time.Duration

class SqlPerformanceReporter {

  private var sinks = emptyList<ReportSink>()

  companion object {
    private val INSTANCE = SqlPerformanceReporter()

    fun addSink(sink: ReportSink) {
      INSTANCE.addSink(sink)
    }

    @JvmStatic
    fun report(
        daoName: String,
        methodName: String,
        timeTaken: Duration
    ) {
      INSTANCE.sendToAll(daoName, methodName, timeTaken)
    }
  }

  private fun sendToAll(
      daoName: String,
      methodName: String,
      timeTaken: Duration
  ) {
    sinks.forEach { sink ->
      try {
        sink.report(daoName, methodName, timeTaken)
      } catch (e: Exception) {
        // We don't need to handle any failures here, but we do want to know if they happened
        Timber.tag("SqlPerformanceReporter").w(e)
      }
    }
  }

  fun addSink(sink: ReportSink) {
    sinks = sinks + sink
  }

  interface ReportSink {
    fun report(
        daoName: String,
        methodName: String,
        timeTaken: Duration
    )
  }
}
