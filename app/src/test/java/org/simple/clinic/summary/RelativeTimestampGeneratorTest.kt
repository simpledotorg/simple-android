package org.simple.clinic.summary

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.util.RelativeTimestamp.*
import org.simple.clinic.util.RelativeTimestampGenerator
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
}
