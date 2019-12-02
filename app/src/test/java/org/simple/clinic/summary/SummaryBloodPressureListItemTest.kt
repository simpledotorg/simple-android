package org.simple.clinic.summary

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.summary.bloodpressures.SummaryBloodPressureListItem
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class SummaryBloodPressureListItemTest {

  private val utcClock = TestUtcClock(Instant.parse("2018-01-01T00:00:00Z"))
  private val userClock = TestUserClock(LocalDate.parse("2018-01-01"))
  private val patientUuid = UUID.fromString("28cdca62-79c9-4af9-b8c2-78cde2cea9e7")
  private val timestampGenerator = RelativeTimestampGenerator()
  private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
  private val dateFormatter = DateTimeFormatter.ISO_INSTANT
  private val zoneId = ZoneOffset.UTC

  @Test
  fun `when a bp is created then it should be editable till a fixed config duration`() {
    // given
    val bpEditableDuration = Duration.ofMinutes(5)

    val now = Instant.parse("2018-01-01T00:00:00Z")
    val bps = listOf(
        PatientMocker.bp(
            uuid = UUID.fromString("2077cea4-c880-4163-a577-74cab23741a6"),
            patientUuid = patientUuid,
            createdAt = now
        ),
        PatientMocker.bp(
            uuid = UUID.fromString("5ed902c6-8c77-4a88-ba57-0cfb31a419fe"),
            patientUuid = patientUuid,
            createdAt = now.minus(bpEditableDuration).minusMillis(1)
        ),
        PatientMocker.bp(
            uuid = UUID.fromString("5ed902c6-8c77-4a88-ba57-0cfb31a419fe"),
            patientUuid = patientUuid,
            createdAt = now.minus(bpEditableDuration)
        )
    )


    // when
    val listItems = SummaryBloodPressureListItem.from(
        bloodPressures = bps,
        timestampGenerator = timestampGenerator,
        dateFormatter = dateFormatter,
        canEditFor = bpEditableDuration,
        bpTimeFormatter = timeFormatter,
        zoneId = zoneId,
        utcClock = utcClock,
        userClock = userClock
    )

    // then
    assertThat(listItems.map { it.isBpEditable }).containsExactly(true, false, true).inOrder()
  }

  @Test
  fun `bp list items should be grouped on the day they were recorded on`() {
    // given
    val bps = listOf(
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.parse("2018-01-01T12:00:00Z")),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.parse("2018-01-01T00:00:00Z")),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.parse("2017-12-31T23:59:59Z")),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.parse("2017-12-31T00:00:00Z")),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.parse("2018-12-30T23:59:59Z"))
    )

    // when
    val listItems = SummaryBloodPressureListItem.from(
        bloodPressures = bps,
        timestampGenerator = timestampGenerator,
        dateFormatter = dateFormatter,
        canEditFor = Duration.ZERO,
        bpTimeFormatter = timeFormatter,
        zoneId = zoneId,
        utcClock = utcClock,
        userClock = userClock
    )

    // then
    assertThat(listItems.map { it.showDivider }).containsExactly(false, true, false, true, true).inOrder()
  }

  @Test
  fun `bp list items should show the exact time they were recorded on if they were recorded on the same day`() {
    // given
    val bps = listOf(
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.parse("2018-01-01T12:45:00Z")),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.parse("2018-01-01T10:30:15Z")),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.parse("2017-12-31T07:15:05Z")),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.parse("2017-12-31T18:43:00Z")),
        PatientMocker.bp(patientUuid = patientUuid, recordedAt = Instant.parse("2018-12-30T23:59:59Z"))
    )

    // when
    val listItems = SummaryBloodPressureListItem.from(
        bloodPressures = bps,
        timestampGenerator = timestampGenerator,
        dateFormatter = dateFormatter,
        canEditFor = Duration.ZERO,
        bpTimeFormatter = timeFormatter,
        zoneId = zoneId,
        utcClock = utcClock,
        userClock = userClock
    )

    // then
    assertThat(listItems.map { it.formattedTime }).containsExactly(
        "12:45 PM",
        "10:30 AM",
        "7:15 AM",
        "6:43 PM",
        null
    ).inOrder()
  }

  @Test
  fun `bp list items should be generated`() {
    fun BloodPressureMeasurement.toSummaryListItem(isEditable: Boolean): SummaryBloodPressureListItem {
      return SummaryBloodPressureListItem(
          measurement = this,
          showDivider = true,
          formattedTime = null,
          addTopPadding = true,
          daysAgo = timestampGenerator.generate(recordedAt, userClock),
          dateFormatter = dateFormatter,
          isBpEditable = isEditable
      )
    }

    // given
    val bpRecordedToday = PatientMocker.bp(
        uuid = UUID.fromString("afe13a4d-feef-412b-b972-7d9bbf2a9fae"),
        patientUuid = patientUuid,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        recordedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val bpRecordedYesterday = PatientMocker.bp(
        uuid = UUID.fromString("010351f5-e7e0-4c3b-a416-f327ab9f8b97"),
        patientUuid = patientUuid,
        createdAt = Instant.parse("2017-12-31T00:00:00Z"),
        recordedAt = Instant.parse("2017-12-31T00:00:00Z")
    )
    val bpRecordedWithinSixMonths = PatientMocker.bp(
        uuid = UUID.fromString("9acfac6a-2155-4996-9055-af17d3104c81"),
        patientUuid = patientUuid,
        createdAt = Instant.parse("2017-11-15T00:00:00Z"),
        recordedAt = Instant.parse("2017-11-15T00:00:00Z")
    )
    val bpRecordedALongTimeAgo = PatientMocker.bp(
        uuid = UUID.fromString("d39c9866-b2b2-4178-a07e-6fe694472c18"),
        patientUuid = patientUuid,
        createdAt = Instant.parse("2016-01-01T00:00:00Z"),
        recordedAt = Instant.parse("2016-01-01T00:00:00Z")
    )

    val bps = listOf(
        bpRecordedToday,
        bpRecordedYesterday,
        bpRecordedWithinSixMonths,
        bpRecordedALongTimeAgo
    )

    // when
    val listItems = SummaryBloodPressureListItem.from(
        bloodPressures = bps,
        timestampGenerator = timestampGenerator,
        dateFormatter = dateFormatter,
        canEditFor = Duration.ZERO,
        bpTimeFormatter = timeFormatter,
        zoneId = zoneId,
        utcClock = utcClock,
        userClock = userClock
    )

    // then
    assertThat(listItems)
        .containsExactly(
            bpRecordedToday.toSummaryListItem(true),
            bpRecordedYesterday.toSummaryListItem(false),
            bpRecordedWithinSixMonths.toSummaryListItem(false),
            bpRecordedALongTimeAgo.toSummaryListItem(false)
        ).inOrder()
  }
}
