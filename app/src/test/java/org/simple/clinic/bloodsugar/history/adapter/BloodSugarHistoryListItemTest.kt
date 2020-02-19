package org.simple.clinic.bloodsugar.history.adapter

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem.BloodSugarHistoryItem
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem.NewBloodSugarButton
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit.DAYS
import org.threeten.bp.temporal.ChronoUnit.MINUTES
import java.util.Locale
import java.util.UUID

class BloodSugarHistoryListItemTest {
  private val userClock = TestUserClock()
  private val utcClock = TestUtcClock()
  private val locale = Locale.ENGLISH
  private val dateFormatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", locale)
  private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", locale)

  @Test
  fun `new blood sugar button should be at the top of the list`() {
    // when
    val bloodSugar1 = PatientMocker.bloodSugar(
        uuid = UUID.fromString("5eb4e57f-69c5-4ade-be4b-5371d0d13bd2"),
        reading = BloodSugarReading(420, Random),
        recordedAt = Instant.parse("2020-01-01T00:00:00Z")
    )
    val bloodSugar2 = PatientMocker.bloodSugar(
        uuid = UUID.fromString("a6027293-5e2b-4289-9ccd-a4befc3be1b3"),
        reading = BloodSugarReading(168, PostPrandial),
        recordedAt = Instant.parse("2020-01-05T00:00:00Z")
    )
    val listItems = BloodSugarHistoryListItem.from(
        listOf(bloodSugar1, bloodSugar2),
        userClock,
        dateFormatter,
        timeFormatter,
        Duration.ofMinutes(10),
        utcClock
    )

    // then
    assertThat(listItems)
        .containsExactly(
            NewBloodSugarButton,
            BloodSugarHistoryItem(measurement = bloodSugar1, bloodSugarDate = "1-Jan-2020", bloodSugarTime = null, isBloodSugarEditable = true),
            BloodSugarHistoryItem(measurement = bloodSugar2, bloodSugarDate = "5-Jan-2020", bloodSugarTime = null, isBloodSugarEditable = true)
        )
  }

  @Test
  fun `if two blood sugars are recorded in same date, then show blood sugar date and time`() {
    // when
    val bloodSugar1 = PatientMocker.bloodSugar(
        uuid = UUID.fromString("b0001467-88b2-44ad-b046-7b7d5f905979"),
        reading = BloodSugarReading(250, PostPrandial),
        recordedAt = Instant.parse("2020-01-16T10:00:00Z")
    )
    val bloodSugar2 = PatientMocker.bloodSugar(
        uuid = UUID.fromString("c1dde486-feef-4e46-a70e-61139ffaf20e"),
        reading = BloodSugarReading(455, Random),
        recordedAt = Instant.parse("2020-01-16T09:00:00Z")
    )
    val bloodSugar3 = PatientMocker.bloodSugar(
        uuid = UUID.fromString("94304037-889b-476c-a95f-ecb4de897d6e"),
        reading = BloodSugarReading(650, Fasting),
        recordedAt = Instant.parse("2019-11-22T00:00:00Z")
    )
    val listItems = BloodSugarHistoryListItem.from(
        listOf(bloodSugar1, bloodSugar2, bloodSugar3),
        userClock,
        dateFormatter,
        timeFormatter,
        Duration.ofMinutes(10),
        utcClock
    )

    // then
    assertThat(listItems)
        .containsExactly(
            NewBloodSugarButton,
            BloodSugarHistoryItem(measurement = bloodSugar1, bloodSugarDate = "16-Jan-2020", bloodSugarTime = "10:00 AM", isBloodSugarEditable = true),
            BloodSugarHistoryItem(measurement = bloodSugar2, bloodSugarDate = "16-Jan-2020", bloodSugarTime = "9:00 AM", isBloodSugarEditable = true),
            BloodSugarHistoryItem(measurement = bloodSugar3, bloodSugarDate = "22-Nov-2019", bloodSugarTime = null, isBloodSugarEditable = true)
        )
  }

  @Test
  fun `if the blood sugar was created within the blood sugar editable duration, then the blood sugar list item must be editable`() {
    // given
    val recordedAt = Instant.parse("2020-01-01T09:00:00Z")
    val createdAt = Instant.parse("2020-01-01T09:00:00Z")
    val testUtcClock = TestUtcClock(createdAt)

    val bloodSugarNow = PatientMocker.bloodSugar(
        uuid = UUID.fromString("c78b2bdf-1d38-439d-b755-91414d76b031"),
        reading = BloodSugarReading(120, Random),
        recordedAt = recordedAt,
        timestamps = Timestamps.create(testUtcClock)
    )
    val bloodSugarInPast = PatientMocker.bloodSugar(
        uuid = UUID.fromString("270cf83f-913d-47c5-ab4d-dd8a742ccad2"),
        reading = BloodSugarReading(156, PostPrandial),
        recordedAt = recordedAt.minus(1, DAYS),
        timestamps = Timestamps(
            createdAt = createdAt.minus(15, MINUTES),
            updatedAt = createdAt.minus(15, MINUTES),
            deletedAt = null
        )
    )
    val listItems = BloodSugarHistoryListItem.from(
        listOf(bloodSugarNow, bloodSugarInPast),
        userClock,
        dateFormatter,
        timeFormatter,
        Duration.ofMinutes(10),
        testUtcClock
    )

    // then
    assertThat(listItems)
        .containsExactly(
            NewBloodSugarButton,
            BloodSugarHistoryItem(measurement = bloodSugarNow, bloodSugarDate = "1-Jan-2020", bloodSugarTime = null, isBloodSugarEditable = true),
            BloodSugarHistoryItem(measurement = bloodSugarInPast, bloodSugarDate = "31-Dec-2019", bloodSugarTime = null, isBloodSugarEditable = false)
        )
  }

}
