package org.simple.clinic.summary

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.summary.bloodpressures.SummaryBloodPressurePlaceholderListItem
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit.DAYS

class SummaryBloodPressurePlaceholderListItemTest {

  private val utcClock = TestUtcClock(Instant.parse("2018-01-01T00:00:00Z"))

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
    val placeholders = SummaryBloodPressurePlaceholderListItem.from(bps, utcClock, 3)

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
    val placeholders = SummaryBloodPressurePlaceholderListItem.from(bps, utcClock, 3)

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
    val placeholders = SummaryBloodPressurePlaceholderListItem.from(bps, utcClock, 3)

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
    val placeholders = SummaryBloodPressurePlaceholderListItem.from(bps, utcClock, 3)

    // then
    assertThat(placeholders).isEmpty()
  }
}
