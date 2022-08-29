package org.simple.clinic.benchmark

import android.util.Log
import io.opentracing.util.GlobalTracer
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.simple.clinic.TestClinicApp
import java.util.concurrent.TimeUnit.MICROSECONDS
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

        val tracer = GlobalTracer.get()
        val adjustedStartTime = millisToMicros(System.currentTimeMillis() - medianTimeTaken)
        val span = tracer
            .buildSpan("test.method")
            .withTag("class", testClass)
            .withTag("method", testMethod)
            .withStartTimestamp(adjustedStartTime)
            .start()

        span.finish(adjustedStartTime + millisToMicros(medianTimeTaken))
      }

      private fun millisToMicros(millis: Long) = MICROSECONDS.convert(millis, MILLISECONDS)
    }
  }
}
