package org.simple.clinic.benchmark

import org.junit.Rule
import org.junit.Test

@BenchmarkTest
class CanaryBenchmarkTest {

  @get:Rule
  val rule = BenchmarkTestRule()

  @Test
  fun benchmark_test_timings_should_be_tracked() {
    Thread.sleep(1500L)
  }
}
