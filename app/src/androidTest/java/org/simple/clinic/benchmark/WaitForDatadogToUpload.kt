package org.simple.clinic.benchmark

import android.os.Bundle
import android.util.Log
import androidx.test.internal.runner.listener.InstrumentationRunListener
import io.opentracing.util.GlobalTracer
import org.junit.runner.Result
import org.simple.clinic.TestClinicApp
import java.io.PrintStream
import java.time.Duration

class WaitForDatadogToUpload : InstrumentationRunListener() {

  override fun instrumentationRunFinished(streamResult: PrintStream?, resultBundle: Bundle?, junitResults: Result?) {
    // We don't have a way to force Datadog to upload all its traces, so we'll wait for a few seconds
    // for DD to upload, and then we'll finish it.
    if (TestClinicApp.isInBenchmarkMode) {
      Log.i("PerfRegression", "Wait for Datadog upload")
      Thread.sleep(Duration.ofMinutes(1).toMillis())
      GlobalTracer.get().close()
    }
  }
}
