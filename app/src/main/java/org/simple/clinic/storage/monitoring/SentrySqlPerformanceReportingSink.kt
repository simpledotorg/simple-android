package org.simple.clinic.storage.monitoring

import io.sentry.ISpan
import io.sentry.Sentry
import io.sentry.TransactionOptions
import org.simple.clinic.storage.monitoring.SqlPerformanceReporter.ReportSink
import org.simple.clinic.storage.monitoring.SqlPerformanceReporter.SqlOperation

class SentrySqlPerformanceReportingSink : ReportSink {

  /**
   * Maintains the list of running spans for every DB call.
   *
   * This can potentially lead to memory leaks if there are DB operations that start but never
   * finish. However, the chances of that seem small enough that it may not be necessary to take
   * on the maintenance effort of periodically removing long running spans.
   *
   * Something to investigate at a later date.
   **/
  private var runningSpans = emptyMap<SqlOperation, ISpan>()

  override fun begin(operation: SqlOperation) {
    val span = Sentry.startTransaction(
        /* name = */ "room.query",
        /* operation = */ "${operation.daoName}/${operation.methodName}"
    )
    span.setTag("op_thread", operation.threadName)

    runningSpans = runningSpans + (operation to span)
  }

  override fun end(operation: SqlOperation) {
    val spanForOperation = runningSpans[operation]

    /*
    * There is a possibility for a completed operation to not be present in the list of running
    * spans since we make copies of the map of running spans and the DB queries can run on multiple
    * threads.
    *
    * In this scenario, we'll just not report this span since the cost of thread synchronisation is
    * not worth the small chances of a span being lost.
    **/
    if (spanForOperation != null) {
      spanForOperation.finish()
      runningSpans = runningSpans - operation
    }
  }
}
