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
    fun begin(
        daoName: String,
        startTimeMillis: Long,
        methodName: String
    ) {
      val operation = SqlOperation(daoName, startTimeMillis, methodName)

      INSTANCE.onEachSink { it.begin(operation) }
    }

    @JvmStatic
    fun end(
        daoName: String,
        startTimeMillis: Long,
        methodName: String
    ) {
      val operation = SqlOperation(daoName, startTimeMillis, methodName)

      INSTANCE.onEachSink { it.end(operation) }
    }
  }

  private inline fun onEachSink(block: (ReportSink) -> Unit) {
    sinks.forEach { sink ->
      try {
        block(sink)
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
    fun begin(operation: SqlOperation)
    fun end(operation: SqlOperation)
  }

  data class SqlOperation(
      val daoName: String,
      val startTimeMillis: Long,
      val methodName: String
  )
}
