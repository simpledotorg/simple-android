package org.simple.clinic.bp.history.adapter

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem.BloodPressureHistoryItem
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem.NewBpButton
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit.DAYS
import org.threeten.bp.temporal.ChronoUnit.MINUTES
import java.util.Locale
import java.util.UUID

class BloodPressureHistoryListItemTest {

  private val userClock = TestUserClock()
  private val locale = Locale.ENGLISH
  private val dateFormatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", locale)
  private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", locale)

  @Test
  fun `if the blood pressure was created within the BP editable duration, then the bp list item must be editable`() {
    // when
    val recordedAt = Instant.parse("2020-01-01T00:00:00Z")
    val createdAt = Instant.parse("2020-01-01T00:00:00Z")
    val bloodPressureNow = PatientMocker.bp(
        uuid = UUID.fromString("78e876ae-3055-43d6-a132-7ad5dd930e23"),
        systolic = 120,
        diastolic = 70,
        createdAt = createdAt,
        recordedAt = recordedAt
    )
    val bloodPressureInPast = PatientMocker.bp(
        uuid = UUID.fromString("2b929a33-e543-4a4f-a98e-5d0d3e8e1e03"),
        systolic = 120,
        diastolic = 70,
        createdAt = createdAt.minus(15, MINUTES),
        recordedAt = recordedAt.minus(1, DAYS)
    )
    val utcClock = TestUtcClock(createdAt)
    val listItems = BloodPressureHistoryListItem.from(
        listOf(bloodPressureNow, bloodPressureInPast),
        Duration.ofMinutes(10),
        utcClock,
        userClock,
        dateFormatter,
        timeFormatter
    )

    // then
    assertThat(listItems)
        .containsExactly(
            NewBpButton,
            BloodPressureHistoryItem(measurement = bloodPressureNow, isBpEditable = true, isHighBloodPressure = false, bpDate = "1-Jan-2020", bpTime = null),
            BloodPressureHistoryItem(measurement = bloodPressureInPast, isBpEditable = false, isHighBloodPressure = false, bpDate = "31-Dec-2019", bpTime = null)
        )
  }

  @Test
  fun `if the blood pressure reading is high, then it must be displayed as a high BP`() {
    // when
    val bloodPressureNormal = PatientMocker.bp(
        uuid = UUID.fromString("78e876ae-3055-43d6-a132-7ad5dd930e23"),
        systolic = 120,
        diastolic = 70,
        recordedAt = Instant.parse("2020-01-06T00:00:00Z")
    )
    val bloodPressureHigh = PatientMocker.bp(
        uuid = UUID.fromString("2b929a33-e543-4a4f-a98e-5d0d3e8e1e03"),
        systolic = 180,
        diastolic = 70,
        recordedAt = Instant.parse("2020-01-08T00:00:00Z")
    )
    val utcClock = TestUtcClock()
    val listItems = BloodPressureHistoryListItem.from(
        listOf(bloodPressureNormal, bloodPressureHigh),
        Duration.ofMinutes(10),
        utcClock,
        userClock,
        dateFormatter,
        timeFormatter
    )

    // then
    assertThat(listItems)
        .containsExactly(
            NewBpButton,
            BloodPressureHistoryItem(measurement = bloodPressureNormal, isBpEditable = true, isHighBloodPressure = false, bpDate = "6-Jan-2020", bpTime = null),
            BloodPressureHistoryItem(measurement = bloodPressureHigh, isBpEditable = true, isHighBloodPressure = true, bpDate = "8-Jan-2020", bpTime = null)
        ).inOrder()
  }

  @Test
  fun `if there is only one list item, then divider should not be shown`() {
    // when
    val utcClock = TestUtcClock()
    val bloodPressure = PatientMocker.bp(
        uuid = UUID.fromString("0606d6b3-96c4-4380-8410-5dae8f3c5059"),
        systolic = 120,
        diastolic = 70,
        recordedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val listItems = BloodPressureHistoryListItem.from(
        listOf(bloodPressure),
        Duration.ofMinutes(10),
        utcClock,
        userClock,
        dateFormatter,
        timeFormatter
    )

    // then
    assertThat(listItems)
        .containsExactly(
            NewBpButton,
            BloodPressureHistoryItem(measurement = bloodPressure, isBpEditable = true, isHighBloodPressure = false, bpDate = "1-Jan-2018", bpTime = null)
        )
        .inOrder()
  }

  @Test
  fun `if there are more than two list items, then dividers should be shown in between`() {
    // when
    val utcClock = TestUtcClock()
    val bloodPressure1 = PatientMocker.bp(
        uuid = UUID.fromString("0606d6b3-96c4-4380-8410-5dae8f3c5059"),
        systolic = 120,
        diastolic = 70,
        recordedAt = Instant.parse("2020-01-16T00:00:00Z")
    )
    val bloodPressure2 = PatientMocker.bp(
        uuid = UUID.fromString("25b10221-5ebd-4025-9274-99fade82af4d"),
        systolic = 180,
        diastolic = 85,
        recordedAt = Instant.parse("2019-12-13T00:00:00Z")
    )
    val bloodPressure3 = PatientMocker.bp(
        uuid = UUID.fromString("3ea1b163-9f98-4fed-80bb-c9096b5bd871"),
        systolic = 120,
        diastolic = 65,
        recordedAt = Instant.parse("2019-11-22T00:00:00Z")
    )
    val listItems = BloodPressureHistoryListItem.from(
        listOf(bloodPressure1, bloodPressure2, bloodPressure3),
        Duration.ofMinutes(10),
        utcClock,
        userClock,
        dateFormatter,
        timeFormatter
    )

    // then
    assertThat(listItems)
        .containsExactly(
            NewBpButton,
            BloodPressureHistoryItem(measurement = bloodPressure1, isBpEditable = true, isHighBloodPressure = false, bpDate = "16-Jan-2020", bpTime = null),
            BloodPressureHistoryItem(measurement = bloodPressure2, isBpEditable = true, isHighBloodPressure = true, bpDate = "13-Dec-2019", bpTime = null),
            BloodPressureHistoryItem(measurement = bloodPressure3, isBpEditable = true, isHighBloodPressure = false, bpDate = "22-Nov-2019", bpTime = null)
        )
        .inOrder()
  }

  @Test
  fun `if two blood pressures are recorded in same date, then show blood pressure date and time`() {
    // when
    val utcClock = TestUtcClock()
    val bloodPressure1 = PatientMocker.bp(
        uuid = UUID.fromString("1f15a240-90ef-4820-91f8-b09c84aa2dd1"),
        systolic = 120,
        diastolic = 70,
        recordedAt = Instant.parse("2020-01-16T10:00:00Z")
    )
    val bloodPressure2 = PatientMocker.bp(
        uuid = UUID.fromString("2a890924-e127-41da-a2d0-172975918181"),
        systolic = 180,
        diastolic = 85,
        recordedAt = Instant.parse("2020-01-16T09:00:00Z")
    )
    val bloodPressure3 = PatientMocker.bp(
        uuid = UUID.fromString("717a3350-b0d6-4356-bd72-de0f70c5b09c"),
        systolic = 120,
        diastolic = 65,
        recordedAt = Instant.parse("2019-12-16T00:00:00Z")
    )
    val listItems = BloodPressureHistoryListItem.from(
        listOf(bloodPressure1, bloodPressure2, bloodPressure3),
        Duration.ofMinutes(10),
        utcClock,
        userClock,
        dateFormatter,
        timeFormatter
    )

    // then
    assertThat(listItems)
        .containsExactly(
            NewBpButton,
            BloodPressureHistoryItem(measurement = bloodPressure1, isBpEditable = true, isHighBloodPressure = false, bpDate = "16-Jan-2020", bpTime = "10:00 AM"),
            BloodPressureHistoryItem(measurement = bloodPressure2, isBpEditable = true, isHighBloodPressure = true, bpDate = "16-Jan-2020", bpTime = "9:00 AM"),
            BloodPressureHistoryItem(measurement = bloodPressure3, isBpEditable = true, isHighBloodPressure = false, bpDate = "16-Dec-2019", bpTime = null)
        )
        .inOrder()
  }
}
