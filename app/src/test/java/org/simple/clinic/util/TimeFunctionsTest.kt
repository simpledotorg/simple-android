package org.simple.clinic.util

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

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
  fun `different clocks should provide correct instant`(
      userClock: UserClock,
      localDate: LocalDate,
      expectedInstant: Instant
  ) {
    assertThat(localDate.toUtcInstant(userClock)).isEqualTo(expectedInstant)
  }

  fun `params for testing conversion of local date to instant`(): List<List<Any>> {
    val userClock = TestUserClock()

    return listOf(
        listOf(userClock, LocalDate.now(userClock), Instant.now(userClock)),
        listOf(userClock, LocalDate.now(userClock).plusDays(2), Instant.now(userClock).plus(2, ChronoUnit.DAYS))
    )
  }
}
