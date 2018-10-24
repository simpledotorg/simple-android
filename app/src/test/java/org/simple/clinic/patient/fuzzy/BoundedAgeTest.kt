package org.simple.clinic.patient.fuzzy

import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import java.lang.AssertionError

@RunWith(JUnitParamsRunner::class)
class BoundedAgeTest {

  @Test(expected = AssertionError::class)
  @Parameters(value = [
  "1991-01-30|1990-01-30",
  "1991-02-28|1991-01-30",
  "1991-01-30|1991-01-28"
  ])
  fun `it should fail if the upper bound is earlier than the lower bound`(
      lower: String,
      upper: String
  ) {
    BoundedAge(lower = LocalDate.parse(lower), upper = LocalDate.parse(upper))
  }

  @Test
  @Parameters(value = [
    "1990-01-30|1991-01-30",
    "1991-01-30|1991-02-28",
    "1991-01-28|1991-01-30",
    "1991-01-28|1991-01-28"
  ])
  fun `it should not fail if the upper bound is after or the same as the lower bound`(
      lower: String,
      upper: String
  ) {
    BoundedAge(lower = LocalDate.parse(lower), upper = LocalDate.parse(upper))
  }
}
