package org.simple.clinic.summary

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.summary.bloodpressures.SummaryBloodPressurePlaceholderListItem
import org.simple.clinic.summary.bloodpressures.SummaryBpItem
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit.DAYS
import java.util.Locale

class SummaryBloodPressurePlaceholderListItemTest {

  private val utcClock = TestUtcClock(Instant.parse("2018-01-01T00:00:00Z"))
  private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
  private val relativeTimestampGenerator = RelativeTimestampGenerator()

  @Test
  fun `bp placeholder items must be generated up to a given limit for bps grouped by day`() {
    // given
    val now = Instant.parse("2018-01-01T00:00:00Z")
    val bps = listOf(
        PatientMocker.bp(recordedAt = now),
        PatientMocker.bp(recordedAt = now),
        PatientMocker.bp(recordedAt = now.minus(1, DAYS))
    )

    // when
    val placeholders = SummaryBpItem.from(
        bloodPressureMeasurements = bps,
        utcClock = utcClock,
        timestampGenerator = relativeTimestampGenerator,
        dateFormatter = mock(),
        canEditFor = Duration.ZERO,
        bpTimeFormatter = timeFormatter,
        zoneId = ZoneOffset.UTC,
        userClock = TestUserClock(),
        placeholderLimit = 3
    ).filterIsInstance<SummaryBloodPressurePlaceholderListItem>()

    // then
    assertThat(placeholders)
        .containsExactly(SummaryBloodPressurePlaceholderListItem(1, false))
        .inOrder()
  }

  @Test
  fun `if there are no bps, the first placeholder item must show a hint`() {
    // given
    val bps = emptyList<BloodPressureMeasurement>()

    // when
    val placeholders = SummaryBpItem.from(
        bloodPressureMeasurements = bps,
        utcClock = utcClock,
        timestampGenerator = relativeTimestampGenerator,
        dateFormatter = mock(),
        canEditFor = Duration.ZERO,
        bpTimeFormatter = timeFormatter,
        zoneId = ZoneOffset.UTC,
        userClock = TestUserClock(),
        placeholderLimit = 3
    ).filterIsInstance<SummaryBloodPressurePlaceholderListItem>()

    // then
    assertThat(placeholders)
        .containsExactly(
            SummaryBloodPressurePlaceholderListItem(1, true),
            SummaryBloodPressurePlaceholderListItem(2, false),
            SummaryBloodPressurePlaceholderListItem(3, false)
        )
        .inOrder()
  }

  @Test
  fun `if there are exactly the same number of bps than the placeholder limit, then no placeholders should be generated`() {
    // given
    val now = Instant.parse("2018-01-01T00:00:00Z")
    val bps = listOf(
        PatientMocker.bp(recordedAt = now),
        PatientMocker.bp(recordedAt = now.minus(1, DAYS)),
        PatientMocker.bp(recordedAt = now.minus(2, DAYS))
    )

    // when
    val placeholders = SummaryBpItem.from(
        bloodPressureMeasurements = bps,
        utcClock = utcClock,
        timestampGenerator = relativeTimestampGenerator,
        dateFormatter = mock(),
        canEditFor = Duration.ZERO,
        bpTimeFormatter = timeFormatter,
        zoneId = ZoneOffset.UTC,
        userClock = TestUserClock(),
        placeholderLimit = 3
    ).filterIsInstance<SummaryBloodPressurePlaceholderListItem>()

    // then
    assertThat(placeholders).isEmpty()
  }

  @Test
  fun `if there are more number of bps than the placeholder limit, then no placeholders should be generated`() {
    // given
    val now = Instant.parse("2018-01-01T00:00:00Z")
    val bps = listOf(
        PatientMocker.bp(recordedAt = now),
        PatientMocker.bp(recordedAt = now.minus(1, DAYS)),
        PatientMocker.bp(recordedAt = now.minus(2, DAYS)),
        PatientMocker.bp(recordedAt = now.minus(3, DAYS))
    )

    // when
    val placeholders = SummaryBpItem.from(
        bloodPressureMeasurements = bps,
        utcClock = utcClock,
        timestampGenerator = relativeTimestampGenerator,
        dateFormatter = mock(),
        canEditFor = Duration.ZERO,
        bpTimeFormatter = timeFormatter,
        zoneId = ZoneOffset.UTC,
        userClock = TestUserClock(),
        placeholderLimit = 3
    ).filterIsInstance<SummaryBloodPressurePlaceholderListItem>()

    // then
    assertThat(placeholders).isEmpty()
  }
}
