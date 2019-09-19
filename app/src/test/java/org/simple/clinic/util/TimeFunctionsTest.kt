package org.simple.clinic.util

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.ChronoUnit

@RunWith(JUnitParamsRunner::class)
class TimeFunctionsTest {

  @Test
  @Parameters(method = "params for testing conversion of Instant to LocalDate at zone")
  fun `different zones should provide the correct local date`(
      zone: ZoneOffset,
      localDate: LocalDate
  ) {
    val clock = TestUtcClock()
    assertThat(Instant.now(clock).toLocalDateAtZone(zone)).isEqualTo(localDate)
  }

  fun `params for testing conversion of Instant to LocalDate at zone`() =
      listOf(
          listOf(ZoneOffset.ofHoursMinutes(5, 30), LocalDate.of(1970, 1, 1)),
          listOf(ZoneOffset.ofHoursMinutes(-5, -30), LocalDate.of(1969, 12, 31))
      )

  @Test
  @Parameters(method = "params for testing conversion of local date to instant")
  fun `different clocks should provide correct instant`(userClock: UserClock, localDate: LocalDate, expectedInstant: Instant) {
    assertThat(localDate.toUtcInstant(userClock)).isEqualTo(expectedInstant)
  }

  fun `params for testing conversion of local date to instant`(): List<List<Any>> {
    val userClock = TestUserClock()

    return listOf(
        listOf(userClock, LocalDate.now(userClock), Instant.now(userClock)),
        listOf(userClock, LocalDate.now(userClock).plusDays(2), Instant.now(userClock).plus(2, ChronoUnit.DAYS))
    )
  }

  @Test
  fun `the current age must be estimated from the recorded age correctly`() {
    // given
    val recordedAge = 30
    val ageRecordedAt = Instant.parse("2018-01-01T00:00:00Z")

    fun estimateAgeAtDate(date: LocalDate): Int {
      val clock = TestUserClock(date)
      return estimateCurrentAge(recordedAge = recordedAge, ageRecordedAtTimestamp = ageRecordedAt, clock = clock)
    }

    // when
    val `estimated age on the same day` = estimateAgeAtDate(LocalDate.parse("2018-01-01"))
    val `estimated age almost a year later` = estimateAgeAtDate(LocalDate.parse("2018-12-31"))
    val `estimated age a year later` = estimateAgeAtDate(LocalDate.parse("2019-01-01"))
    val `estimated age a year and six months later` = estimateAgeAtDate(LocalDate.parse("2019-07-01"))
    val `estimated age two years later` = estimateAgeAtDate(LocalDate.parse("2020-01-01"))
    val `estimated age two years and nine months later` = estimateAgeAtDate(LocalDate.parse("2020-10-01"))
    val `estimated age three years and a day later` = estimateAgeAtDate(LocalDate.parse("2021-01-02"))

    // then
    assertThat(`estimated age on the same day`).isEqualTo(30)
    assertThat(`estimated age almost a year later`).isEqualTo(30)
    assertThat(`estimated age a year later`).isEqualTo(31)
    assertThat(`estimated age a year and six months later`).isEqualTo(31)
    assertThat(`estimated age two years later`).isEqualTo(32)
    assertThat(`estimated age two years and nine months later`).isEqualTo(32)
    assertThat(`estimated age three years and a day later`).isEqualTo(33)
  }

  @Test
  fun `the current age must be estimated from the recorded date correctly`() {
    // given
    val recordedDateOfBirth = LocalDate.parse("1988-01-01")

    fun estimateAgeAtDate(date: LocalDate): Int {
      val clock = TestUserClock(date)
      return estimateCurrentAge(recordedDateOfBirth = recordedDateOfBirth, clock = clock)
    }

    // when
    val `estimated age on the same day` = estimateAgeAtDate(LocalDate.parse("2018-01-01"))
    val `estimated age almost a year later` = estimateAgeAtDate(LocalDate.parse("2018-12-31"))
    val `estimated age a year later` = estimateAgeAtDate(LocalDate.parse("2019-01-01"))
    val `estimated age a year and six months later` = estimateAgeAtDate(LocalDate.parse("2019-07-01"))
    val `estimated age two years later` = estimateAgeAtDate(LocalDate.parse("2020-01-01"))
    val `estimated age two years and nine months later` = estimateAgeAtDate(LocalDate.parse("2020-10-01"))
    val `estimated age three years and a day later` = estimateAgeAtDate(LocalDate.parse("2021-01-02"))

    // then
    assertThat(`estimated age on the same day`).isEqualTo(30)
    assertThat(`estimated age almost a year later`).isEqualTo(30)
    assertThat(`estimated age a year later`).isEqualTo(31)
    assertThat(`estimated age a year and six months later`).isEqualTo(31)
    assertThat(`estimated age two years later`).isEqualTo(32)
    assertThat(`estimated age two years and nine months later`).isEqualTo(32)
    assertThat(`estimated age three years and a day later`).isEqualTo(33)
  }
}
