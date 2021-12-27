package org.simple.clinic.benchmark

import org.junit.runner.Description
import org.junit.runner.manipulation.Filter
import org.simple.clinic.TestClinicApp

@Suppress("unused")
class SelectBenchmarkTests : Filter() {

  private val runningInBenchmarkMode
    get() = TestClinicApp.isInBenchmarkMode

  override fun shouldRun(description: Description): Boolean {
    val isTestClassABenchmarkTest = description
        .testClass
        .annotations
        .any { it is BenchmarkTest }

    return if (runningInBenchmarkMode) isTestClassABenchmarkTest else !isTestClassABenchmarkTest
  }

  override fun describe() = if (runningInBenchmarkMode) "benchmark tests" else "regular tests"
}
