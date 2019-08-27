package org.simple.clinic.summary

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.util.RelativeTimestamp.ExactDate
import org.simple.clinic.util.RelativeTimestamp.Today
import org.simple.clinic.util.RelativeTimestamp.WithinSixMonths
import org.simple.clinic.util.RelativeTimestamp.Yesterday
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.TestUserClock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

class RelativeTimestampGeneratorTest {

  private val generator = RelativeTimestampGenerator()

  @Test
  fun `generated timestamps should be correct`() {

    val todayDateTime = LocalDateTime.of(2018, 7, 29, 16, 58)

    val todayTime = LocalDateTime.of(2018, 7, 29, 2, 0, 0).atOffset(ZoneOffset.UTC).toInstant()
    assertThat(generator.generate(todayDateTime, todayTime)).isInstanceOf(Today::class.java)

    val yesterdayButWithin24Hours = LocalDateTime.of(2018, 7, 28, 23, 0, 0).atOffset(ZoneOffset.UTC).toInstant()
    assertThat(generator.generate(todayDateTime, yesterdayButWithin24Hours)).isInstanceOf(Yesterday::class.java)

    val yesterdayButOutside24Hours = LocalDateTime.of(2018, 7, 28, 3, 0, 0).atOffset(ZoneOffset.UTC).toInstant()
    assertThat(generator.generate(todayDateTime, yesterdayButOutside24Hours)).isInstanceOf(Yesterday::class.java)

    val withinSixMonths = LocalDateTime.of(2018, 3, 29, 4, 0, 0).atOffset(ZoneOffset.UTC).toInstant()
    val withinSixMonthsTimestamp = generator.generate(todayDateTime, withinSixMonths)
    assertThat(withinSixMonthsTimestamp).isEqualTo(WithinSixMonths(122))

    val olderThanSixMonths = LocalDateTime.of(2017, 1, 27, 3, 0, 0).atOffset(ZoneOffset.UTC).toInstant()
    assertThat(generator.generate(todayDateTime, olderThanSixMonths)).isInstanceOf(ExactDate::class.java)

    val tomorrow = todayDateTime.plusDays(1).atOffset(ZoneOffset.UTC).toInstant()
    val tomorrowTimestamp = generator.generate(todayDateTime, tomorrow)
    assertThat(tomorrowTimestamp).isEqualTo(ExactDate(tomorrow))
  }

  @Test
  fun `verify RelativeTimestamp does not produce yesterday incorrectly`() {
    val todayDateTime = LocalDateTime.parse("2019-05-29T00:04:51.402")

    val twoDaysOld = LocalDateTime.parse("2019-05-27T09:51:43.623").atOffset(ZoneOffset.UTC).toInstant()
    val twoDaysOldTimestamp = generator.generate(todayDateTime, twoDaysOld)
    assertThat(twoDaysOldTimestamp).isEqualTo(WithinSixMonths(2))
  }

  @Test
  fun `when the given timestamp is on the current calendar date, the relative timestamp generated must be Today`() {
    // given
    val clock = TestUserClock(LocalDate.parse("2018-01-01"))
    val instantAtBeginningOfDay = Instant.parse("2018-01-01T00:00:00.000Z")
    val instantAtEndOfDay = Instant.parse("2018-01-01T23:59:59.999Z")

    // when
    val timestampAtBeginningOfDay = generator.generate(instantAtBeginningOfDay, clock)
    val timestampAtEndOfDay = generator.generate(instantAtEndOfDay, clock)

    // then
    assertThat(timestampAtBeginningOfDay).isEqualTo(Today)
    assertThat(timestampAtEndOfDay).isEqualTo(Today)
  }

  @Test
  fun `when the given timestamp is on the previous calendar date, the relative timestamp generated must be Yesterday`() {
    // given
    val clock = TestUserClock(LocalDate.parse("2018-01-01"))
    val instantAtBeginningOfPreviousDay = Instant.parse("2017-12-31T00:00:00.000Z")
    val instantAtEndOfPreviousDay = Instant.parse("2017-12-31T23:59:59.999Z")

    // when
    val timestampAtBeginningOfPreviousDay = generator.generate(instantAtBeginningOfPreviousDay, clock)
    val timestampAtEndOfPreviousDay = generator.generate(instantAtEndOfPreviousDay, clock)

    // then
    assertThat(timestampAtBeginningOfPreviousDay).isEqualTo(Yesterday)
    assertThat(timestampAtEndOfPreviousDay).isEqualTo(Yesterday)
  }

  @Test
  fun `when the given timestamp is within six months in the past, the relative timestamp generated must be WithinSixMonths`() {
    // given
    val clock = TestUserClock(LocalDate.parse("2018-01-01"))
    val instantAtBeginningOfSixMonthPeriod = Instant.parse("2017-07-01T00:00:00.000Z")
    val instantInTheMiddleOfSixMonthPeriod = Instant.parse("2017-09-01T00:00:00.000Z")
    val instantAtEndOfSixMonthPeriod = Instant.parse("2017-12-30T23:59:59.999Z")

    // when
    val timestampAtBeginningOfSixMonthPeriod = generator.generate(instantAtBeginningOfSixMonthPeriod, clock)
    val timestampInTheMiddleOfSixMonthPeriod = generator.generate(instantInTheMiddleOfSixMonthPeriod, clock)
    val timestampInTheEndOfSixMonthPeriod = generator.generate(instantAtEndOfSixMonthPeriod, clock)

    // then
    assertThat(timestampAtBeginningOfSixMonthPeriod).isEqualTo(WithinSixMonths(184))
    assertThat(timestampInTheMiddleOfSixMonthPeriod).isEqualTo(WithinSixMonths(122))
    assertThat(timestampInTheEndOfSixMonthPeriod).isEqualTo(WithinSixMonths(2))
  }

  @Test
  fun `when the given timestamp is more than six months in the past, the relative timestamp generated must be ExactDate`() {
    // given
    val clock = TestUserClock(LocalDate.parse("2018-01-01"))
    val instantJustBeforeSixMonthPeriod = Instant.parse("2017-06-30T23:59:59.999Z")
    val instantTwoYearsAway = Instant.parse("2016-01-01T00:00:00.000Z")

    // when
    val timestampJustBeforeSixMonthPeriod = generator.generate(instantJustBeforeSixMonthPeriod, clock)
    val timestampTwoYearsAway = generator.generate(instantTwoYearsAway, clock)

    // then
    assertThat(timestampJustBeforeSixMonthPeriod).isEqualTo(ExactDate(instantJustBeforeSixMonthPeriod))
    assertThat(timestampTwoYearsAway).isEqualTo(ExactDate(instantTwoYearsAway))
  }

  @Test
  fun `when the given timestamp is in the future, the relative timestamp generated must be ExactDate`() {
    // given
    val clock = TestUserClock(LocalDate.parse("2018-01-01"))
    val instantJustAfterToday = Instant.parse("2018-01-02T00:00:00.001Z")
    val instantTwoYearsAfterToday = Instant.parse("2020-01-01T00:00:00.000Z")

    // when
    val timestampJustAfterToday = generator.generate(instantJustAfterToday, clock)
    val timestampTwoYearsAfterToday = generator.generate(instantTwoYearsAfterToday, clock)

    // then
    assertThat(timestampJustAfterToday).isEqualTo(ExactDate(instantJustAfterToday))
    assertThat(timestampTwoYearsAfterToday).isEqualTo(ExactDate(instantTwoYearsAfterToday))
  }
}
