package org.simple.clinic.benchmark

import android.os.Bundle
import android.util.Log
import androidx.test.internal.runner.listener.InstrumentationRunListener
import org.junit.runner.Result
import org.simple.clinic.TestClinicApp
import java.io.PrintStream
import java.time.Duration

class WaitForSentryToUpload : InstrumentationRunListener() {

  override fun instrumentationRunFinished(streamResult: PrintStream?, resultBundle: Bundle?, junitResults: Result?) {
    // We don't have a way to force Sentry to upload all its traces, so we'll wait for a few seconds
    // for Sentry to upload, and then we'll finish it.
    if (TestClinicApp.isInBenchmarkMode) {
      Log.i("PerfRegression", "Wait for Sentry upload")
      Thread.sleep(Duration.ofMinutes(1).toMillis())
    }
  }
}
