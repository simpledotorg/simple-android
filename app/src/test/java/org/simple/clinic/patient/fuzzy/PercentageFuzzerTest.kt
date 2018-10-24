package org.simple.clinic.patient.fuzzy

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.util.TestClock
import org.threeten.bp.LocalDate

@RunWith(JUnitParamsRunner::class)
class PercentageFuzzerTest {

  private val clock = TestClock()

  @Before
  fun setUp() {
    clock.setYear(2000)
  }

  @Test
  @Parameters(
      value = [
        "30|0.1|1967-01-02|1972-12-31",
        "30|0.3|1961-01-01|1979-01-01",
        "30|0.7|1949-01-01|1991-01-01",
        "43|0.1|1952-09-14|1961-04-20",
        "43|0.3|1944-02-08|1969-11-25",
        "43|0.7|1926-11-26|1987-02-07"
      ]
  )
  fun `it should generate upper and lower date bounds`(
      age: Int,
      fuzziness: Float,
      expectedLower: String,
      expectedUpper: String
  ) {
    val (expectedLowerDate, expectedUpperDate) = BoundedAge(lower = LocalDate.parse(expectedLower), upper = LocalDate.parse(expectedUpper))

    val fuzzer = PercentageFuzzer(clock, fuzziness)
    val (lowerDate, upperDate) = fuzzer.bounded(age)

    assertThat(lowerDate).isEqualTo(expectedLowerDate)
    assertThat(upperDate).isEqualTo(expectedUpperDate)
  }

  @Parameters(value = [
    "0|0.1",
    "-1|0.1",
    "1|-0.1",
    "-1|-0.1",
    "10|1.1",
    "10|10.0"
  ])
  @Test(expected = AssertionError::class)
  fun `it should fail on invalid input`(
      age: Int,
      fuzziness: Float
  ) {
    val fuzzer = PercentageFuzzer(clock, fuzziness)

    fuzzer.bounded(age)
  }
}
