package org.simple.clinic.patient.fuzzy

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.util.TestClock
import org.threeten.bp.LocalDate
import java.lang.AssertionError

@RunWith(JUnitParamsRunner::class)
class AbsoluteFuzzerTest {

  private val clock = TestClock()

  @Before
  fun setUp() {
    clock.setYear(2000)
  }

  @Test
  @Parameters(value = [
    "0|3|1997-01-01|2003-01-01",
    "30|3|1967-01-01|1973-01-01",
    "30|5|1965-01-01|1975-01-01"
  ])
  fun `it should properly generate upper and lower date bounds`(
      age: Int,
      fuzziness: Int,
      expectedLower: String,
      expectedUpper: String
  ) {
    val (expectedLowerDate, expectedUpperDate) = BoundedAge(lower = LocalDate.parse(expectedLower), upper = LocalDate.parse(expectedUpper))

    val fuzzer = AbsoluteFuzzer(clock, fuzziness)
    val (lowerDate, upperDate) = fuzzer.bounded(age)

    assertThat(lowerDate).isEqualTo(expectedLowerDate)
    assertThat(upperDate).isEqualTo(expectedUpperDate)
  }

  @Test(expected = AssertionError::class)
  @Parameters(value = [
  "-1|3",
  "1|-3",
  "-1|-3"
  ])
  fun `it should fail on invalid input`(
      age: Int,
      fuzziness: Int
  ) {
    val fuzzer = AbsoluteFuzzer(clock, fuzziness)
    fuzzer.bounded(age)
  }
}
