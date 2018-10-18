package org.simple.clinic.patient.fuzzy

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class AbsoluteFuzzerTest {

  @Parameters(value = [
    "0|3|0|3",
    "1|3|0|4",
    "3|3|0|6",
    "4|3|1|7",
    "0|5|0|5",
    "1|5|0|6",
    "12|5|7|17",
    "15|0|15|15"
  ])
  @Test
  fun `it should properly generate upper and lower age bounds`(
      age: Int,
      fuzziness: Int,
      expectedLower: Int,
      expectedUpper: Int
  ) {
    val fuzzer = AbsoluteFuzzer(fuzziness)

    val (lower, upper) = fuzzer.bounded(age)

    assertThat(age).isIn(lower..upper)
    assertThat(lower).isEqualTo(expectedLower)
    assertThat(upper).isEqualTo(expectedUpper)
  }

  @Parameters(value = [
    "-1|3",
    "1|-3",
    "-1|-3"
  ])
  @Test(expected = AssertionError::class)
  fun `it should fail on invalid input`(
      age: Int,
      fuzziness: Int
  ) {
    val fuzzer = AbsoluteFuzzer(fuzziness)
    fuzzer.bounded(age)
  }
}
