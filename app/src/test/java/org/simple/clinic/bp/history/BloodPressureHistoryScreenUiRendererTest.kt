package org.simple.clinic.bp.history

import androidx.paging.PagingData
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.sharedTestCode.TestData
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class BloodPressureHistoryScreenUiRendererTest {

  private val ui = mock<BloodPressureHistoryScreenUi>()
  private val renderer = BloodPressureHistoryScreenUiRenderer(ui)
  private val patientUuid = UUID.fromString("9dd563b5-99a5-4f43-b3ab-47c43ed5d62c")
  private val emptyPagingData = PagingData.empty<BloodPressureHistoryListItem>()
  private val defaultModel = BloodPressureHistoryScreenModel.create(patientUuid)
      .bloodPressuresLoaded(emptyPagingData)

  @Test
  fun `when blood pressure history is being loaded then do nothing`() {
    //when
    renderer.render(defaultModel)

    //then
    verify(ui).showBloodPressures(emptyPagingData)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient is loaded, then show it on the ui`() {
    //given
    val patient = TestData.patient(
        uuid = UUID.fromString("c80bce99-82bc-4d12-a85a-dcae373fece3")
    )

    //when
    renderer.render(defaultModel.patientLoaded(patient))

    //then
    verify(ui).showPatientInformation(patient)
    verify(ui).showBloodPressures(emptyPagingData)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when blood pressure history is loaded, then show blood pressures`() {
    // given
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

    // when
    renderer.render(defaultModel.bloodPressuresLoaded(bloodPressures = bloodPressures))

    // then
    verify(ui).showBloodPressures(bloodPressures)
    verifyNoMoreInteractions(ui)
  }
}
