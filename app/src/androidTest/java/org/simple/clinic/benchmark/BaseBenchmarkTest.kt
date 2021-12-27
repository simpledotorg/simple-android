package org.simple.clinic.benchmark

import org.junit.Rule

@BenchmarkTest
abstract class BaseBenchmarkTest {

  @get:Rule
  val rule = BenchmarkTestRule()
}
