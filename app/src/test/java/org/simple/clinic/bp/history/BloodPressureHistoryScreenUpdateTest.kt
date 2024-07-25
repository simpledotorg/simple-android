package org.simple.clinic.bp.history

import androidx.paging.PagingData
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.sharedTestCode.TestData
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class BloodPressureHistoryScreenUpdateTest {
  private val patientUuid = UUID.fromString("6b455aea-1580-4900-9524-c19a4d3db676")
  private val model = BloodPressureHistoryScreenModel.create(patientUuid)
  private val updateSpec = UpdateSpec<BloodPressureHistoryScreenModel, BloodPressureHistoryScreenEvent, BloodPressureHistoryScreenEffect>(BloodPressureHistoryScreenUpdate())

  @Test
  fun `when patient is loaded, then show patient information`() {
    val patient = TestData.patient(uuid = UUID.fromString("7d04777d-3480-451b-b571-0e114c87cebf"))

    updateSpec
        .given(model)
        .whenEvent(PatientLoaded(patient))
        .then(
            assertThatNext(
                hasModel(model.patientLoaded(patient)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when add new blood pressure is clicked, then open entry sheet`() {
    updateSpec
        .given(model)
        .whenEvent(NewBloodPressureClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenBloodPressureEntrySheet(patientUuid) as BloodPressureHistoryScreenEffect)
            )
        )
  }

  @Test
  fun `when blood pressure is clicked, then open update sheet`() {
    val bloodPressureMeasurement = TestData.bloodPressureMeasurement(
        UUID.fromString("e42d25fb-5693-449c-9ad2-71a172eb8d92"),
        patientUuid
    )

    updateSpec
        .given(model)
        .whenEvent(BloodPressureClicked(bloodPressureMeasurement))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenBloodPressureUpdateSheet(bloodPressureMeasurement) as BloodPressureHistoryScreenEffect)
            )
        )
  }

  @Test
  fun `when blood pressure history is loaded, then update state`() {
    val patientUuid = UUID.fromString("f6f60760-b290-4e0f-9db0-74179b7cd170")
    val createdAt = Instant.parse("2020-01-01T00:00:00Z")
    val recordedAt = Instant.parse("2020-01-01T00:00:00Z")

    val bloodPressureNow = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("78e876ae-3055-43d6-a132-7ad5dd930e23"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 70,
        createdAt = createdAt,
        recordedAt = recordedAt
    )

    val bloodPressure15MinutesInPast = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("2b929a33-e543-4a4f-a98e-5d0d3e8e1e03"),
        patientUuid = patientUuid,
        systolic = 120,
        diastolic = 85,
        createdAt = createdAt.minus(15, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodPressures: PagingData<BloodPressureHistoryListItem> = PagingData.from(listOf(
        BloodPressureHistoryListItem.BloodPressureHistoryItem(
            measurement = bloodPressureNow,
            isBpEditable = true,
            isBpHigh = false,
            bpDate = "1-Jan-2020",
            bpTime = null
        ),
        BloodPressureHistoryListItem.BloodPressureHistoryItem(
            measurement = bloodPressure15MinutesInPast,
            isBpEditable = false,
            isBpHigh = false,
            bpDate = "31-Dec-2019",
            bpTime = "12:00 AM"
        ),
    ))

    updateSpec
        .given(model)
        .whenEvent(BloodPressuresHistoryLoaded(bloodPressures))
        .then(assertThatNext(
            hasModel(model.bloodPressuresLoaded(bloodPressures = bloodPressures)),
            hasNoEffects()
        ))
  }
}
