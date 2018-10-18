package org.simple.clinic.patient.fuzzy

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class PercentageFuzzerTest {

  @Parameters(value = [
    "0|0.1|0|0",
    "5|0.1|4|5",
    "5|0.5|2|7",
    "5|0.8|1|9",
    "5|1.0|0|10",
    "25|0.1|22|27",
    "25|0.5|12|37",
    "25|0.8|5|45",
    "25|1.0|0|50",
    "43|0.1|38|47",
    "43|0.5|21|64",
    "43|0.8|8|77",
    "43|1.0|0|86"
  ])
  @Test
  fun `it should properly generate upper and lower bounds`(
      age: Int,
      fuzziness: Float,
      expectedLower: Int,
      expectedUpper: Int
  ) {
    val fuzzer = PercentageFuzzer(fuzziness)

    val (lower, upper) = fuzzer.bounded(age)

    assertThat(age).isIn(lower..upper)
    assertThat(lower).isEqualTo(expectedLower)
    assertThat(upper).isEqualTo(expectedUpper)
  }

  @Parameters(value = [
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
    val fuzzer = PercentageFuzzer(fuzziness)

    fuzzer.bounded(age)
  }
}
