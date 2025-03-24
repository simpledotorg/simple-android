package org.simple.clinic.benchmark

import android.util.Log
import io.sentry.Sentry
import io.sentry.SentryLongDate
import io.sentry.SpanStatus
import io.sentry.TransactionOptions
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.simple.clinic.TestClinicApp
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

class BenchmarkTestRule(
    // Default value chosen as 11 because that's what the Android benchmark library also uses.
    private val benchmarkSampleSize: Int = 11
) : TestRule {

  @Inject
  lateinit var backupDatabase: BackupBenchmarkDatabase

  init {
    TestClinicApp.appComponent().inject(this)
  }

  override fun apply(
      base: Statement,
      description: Description
  ): Statement {

    return if (TestClinicApp.isInBenchmarkMode) {
      benchmarkStatement(base, description)
    } else {
      throw RuntimeException("Benchmark test rule should not be executed as part of non benchmark tests")
    }
  }

  private fun benchmarkStatement(
      base: Statement,
      description: Description
  ): Statement {
    val stats = DescriptiveStatistics()

    return object : Statement() {

      override fun evaluate() {
        (1..benchmarkSampleSize).forEach { runNumber ->
          // Restore the database
          backupDatabase.restore()

          val startedAt = System.currentTimeMillis()
          base.evaluate()
          val timeTaken = System.currentTimeMillis() - startedAt

          if (runNumber > 1) {
            // Drop the 1st run since local frameworks might end up setting up caches and connection
            // pools which might inflate the first run
            stats.addValue(timeTaken.toDouble())
          }
        }

        val testClass = description.className
        val testMethod = description.methodName
        val medianTimeTaken = stats.getPercentile(50.0).toLong()

        Log.i("PerfRegression", "Median benchmark for $testClass#$testMethod: ${medianTimeTaken}ms")

        val adjustedStartTime = millisToNanos(System.currentTimeMillis() - medianTimeTaken)

        val span = Sentry.startTransaction(
            /* name = */ "test.method",
            /* operation = */ "$testClass/$testMethod",
            /* transactionOptions = */ TransactionOptions().apply {
              startTimestamp = SentryLongDate(adjustedStartTime)
            }
        )

        span.finish(
            SpanStatus.OK,
            SentryLongDate(adjustedStartTime + millisToNanos(medianTimeTaken)))
      }

      private fun millisToNanos(millis: Long) = TimeUnit.NANOSECONDS.convert(millis, MILLISECONDS)
    }
  }
}
