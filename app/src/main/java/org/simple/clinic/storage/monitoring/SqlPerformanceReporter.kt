package org.simple.clinic.storage.monitoring

import java.time.Duration

class SqlPerformanceReporter {

  companion object {

    @JvmStatic
    fun report(
        daoName: String,
        methodName: String,
        timeTaken: Duration
    ) {

    }
  }
}
