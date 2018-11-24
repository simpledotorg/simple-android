package org.simple.clinic.util

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import org.threeten.bp.temporal.ChronoUnit

@RunWith(JUnitParamsRunner::class)
class TimeUtilTest {

  val clock = TestClock()

  @Before
  fun setUp() {
    clock.resetToEpoch()
  }

  @Test
  @Parameters(method = "params for estimating current age")
  fun `estimated ages should be calculated properly`(
      year: Int,
      recordedAge: Int,
      ageRecordedAt: Instant,
      advanceClockBy: Duration,
      expectedEstimatedAge: Int
  ) {
    clock.setYear(year)
    clock.advanceBy(advanceClockBy)
    val estimatedAge = estimateCurrentAge(recordedAge, ageRecordedAt, clock)

    assertThat(estimatedAge).isEqualTo(expectedEstimatedAge)
  }

  @Suppress("Unused")
  private fun `params for estimating current age`(): List<List<Any>> {
    val oneYear = Period.ofYears(1)
    val twoYears = Period.ofYears(2)
    val thirtyDays = Period.ofDays(30)

    fun daysBetweenNowAndPeriod(period: Period): Duration {
      val now = LocalDate.now(clock)
      val then = LocalDate.now(clock).plus(period)

      return Duration.ofDays(ChronoUnit.DAYS.between(now, then))
    }

    fun generateTestData(
        year: Int,
        age: Int,
        advanceClockBy: Period,
        turnBackAgeRecordedAtBy: Period,
        expectedEstimatedAge: Int
    ): List<Any> {
      clock.setYear(year)

      return listOf(
          year,
          age,
          Instant.now(clock).minus(daysBetweenNowAndPeriod(turnBackAgeRecordedAtBy)),
          daysBetweenNowAndPeriod(advanceClockBy),
          expectedEstimatedAge)
    }
    return listOf(
        generateTestData(
            year = 1970,
            age = 40,
            advanceClockBy = Period.ZERO,
            expectedEstimatedAge = 40,
            turnBackAgeRecordedAtBy = Period.ZERO),
        generateTestData(
            year = 1970,
            age = 40,
            advanceClockBy = thirtyDays,
            expectedEstimatedAge = 40,
            turnBackAgeRecordedAtBy = Period.ZERO),
        generateTestData(
            year = 1970,
            age = 40,
            advanceClockBy = oneYear,
            expectedEstimatedAge = 41,
            turnBackAgeRecordedAtBy = Period.ZERO),
        generateTestData(
            year = 1970,
            age = 25,
            advanceClockBy = Period.ZERO,
            expectedEstimatedAge = 26,
            turnBackAgeRecordedAtBy = oneYear),
        generateTestData(
            year = 1971,
            age = 25,
            advanceClockBy = Period.ZERO,
            expectedEstimatedAge = 27,
            turnBackAgeRecordedAtBy = twoYears)
    )
  }
}
