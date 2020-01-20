package org.simple.clinic.bp.history.adapter

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem.BloodPressureHistoryItem
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit.MINUTES
import java.util.UUID

class BloodPressureHistoryListItemTest {

  @Test
  fun `if the blood pressure was created within the BP editable duration, then the bp list item must be editable`() {
    // when
    val createdAt = Instant.parse("2020-01-01T00:00:00Z")
    val bloodPressureNow = PatientMocker.bp(
        uuid = UUID.fromString("78e876ae-3055-43d6-a132-7ad5dd930e23"),
        systolic = 120,
        diastolic = 70,
        createdAt = createdAt
    )
    val bloodPressureInPast = PatientMocker.bp(
        uuid = UUID.fromString("2b929a33-e543-4a4f-a98e-5d0d3e8e1e03"),
        systolic = 120,
        diastolic = 70,
        createdAt = createdAt.minus(15, MINUTES)
    )
    val utcClock = TestUtcClock(createdAt)
    val listItems = BloodPressureHistoryListItem.from(listOf(bloodPressureNow, bloodPressureInPast), Duration.ofMinutes(10), utcClock)

    // then
    assertThat(listItems)
        .containsExactly(
            BloodPressureHistoryItem(measurement = bloodPressureNow, isBpEditable = true, isHighBloodPressure = false, showDivider = true),
            BloodPressureHistoryItem(measurement = bloodPressureInPast, isBpEditable = false, isHighBloodPressure = false, showDivider = false)
        )
  }

  @Test
  fun `if the blood pressure reading is high, then it must be displayed as a high BP`() {
    // when
    val bloodPressureNormal = PatientMocker.bp(
        uuid = UUID.fromString("78e876ae-3055-43d6-a132-7ad5dd930e23"),
        systolic = 120,
        diastolic = 70
    )
    val bloodPressureHigh = PatientMocker.bp(
        uuid = UUID.fromString("2b929a33-e543-4a4f-a98e-5d0d3e8e1e03"),
        systolic = 180,
        diastolic = 70
    )
    val utcClock = TestUtcClock()
    val listItems = BloodPressureHistoryListItem.from(listOf(bloodPressureNormal, bloodPressureHigh), Duration.ofMinutes(10), utcClock)

    // then
    assertThat(listItems)
        .containsExactly(
            BloodPressureHistoryItem(measurement = bloodPressureNormal, isBpEditable = true, isHighBloodPressure = false, showDivider = true),
            BloodPressureHistoryItem(measurement = bloodPressureHigh, isBpEditable = true, isHighBloodPressure = true, showDivider = false)
        ).inOrder()
  }

  @Test
  fun `if there is only one list item, then divider should not be shown`() {
    // when
    val utcClock = TestUtcClock()
    val bloodPressure = PatientMocker.bp(
        uuid = UUID.fromString("0606d6b3-96c4-4380-8410-5dae8f3c5059"),
        systolic = 120,
        diastolic = 70
    )
    val listItems = BloodPressureHistoryListItem.from(listOf(bloodPressure), Duration.ofMinutes(10), utcClock)

    // then
    assertThat(listItems)
        .containsExactly(
            BloodPressureHistoryItem(measurement = bloodPressure, isBpEditable = true, isHighBloodPressure = false, showDivider = false)
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
        diastolic = 70
    )
    val bloodPressure2 = PatientMocker.bp(
        uuid = UUID.fromString("25b10221-5ebd-4025-9274-99fade82af4d"),
        systolic = 180,
        diastolic = 85
    )
    val bloodPressure3 = PatientMocker.bp(
        uuid = UUID.fromString("3ea1b163-9f98-4fed-80bb-c9096b5bd871"),
        systolic = 120,
        diastolic = 65
    )
    val listItems = BloodPressureHistoryListItem.from(listOf(bloodPressure1, bloodPressure2, bloodPressure3), Duration.ofMinutes(10), utcClock)

    // then
    assertThat(listItems)
        .containsExactly(
            BloodPressureHistoryItem(measurement = bloodPressure1, isBpEditable = true, isHighBloodPressure = false, showDivider = true),
            BloodPressureHistoryItem(measurement = bloodPressure2, isBpEditable = true, isHighBloodPressure = true, showDivider = true),
            BloodPressureHistoryItem(measurement = bloodPressure3, isBpEditable = true, isHighBloodPressure = false, showDivider = false)
        )
        .inOrder()
  }
}
