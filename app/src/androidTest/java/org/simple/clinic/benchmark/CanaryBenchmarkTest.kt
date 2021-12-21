package org.simple.clinic.benchmark

import org.junit.Test

class CanaryBenchmarkTest : BaseBenchmarkTest() {

  @Test
  fun benchmark_test_timings_should_be_tracked() {
    Thread.sleep(1500L)
  }
}
