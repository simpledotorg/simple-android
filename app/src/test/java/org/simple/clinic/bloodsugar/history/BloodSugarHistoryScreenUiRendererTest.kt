package org.simple.clinic.bloodsugar.history

import androidx.paging.PagingData
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem
import org.simple.clinic.TestData
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class BloodSugarHistoryScreenUiRendererTest {
  private val ui = mock<BloodSugarHistoryScreenUi>()
  private val renderer = BloodSugarHistoryScreenUiRenderer(ui)
  private val patientUuid = UUID.fromString("74bc8b07-1d47-4595-9fe8-f1c7c83a1f2a")
  private val emptyPagingData = PagingData.empty<BloodSugarHistoryListItem>()
  private val defaultModel = BloodSugarHistoryScreenModel.create(patientUuid)
      .bloodSugarLoaded(emptyPagingData)

  @Test
  fun `when blood sugar history is being loaded then do nothing`() {
    // when
    renderer.render(defaultModel)

    // then
    verify(ui).showBloodSugars(emptyPagingData)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient is loaded, then show it on the ui`() {
    // given
    val patient = TestData.patient(
        uuid = UUID.fromString("e25d34e8-a17f-4a62-80e5-554d4048694e")
    )

    // when
    renderer.render(defaultModel.patientLoaded(patient))

    // then
    verify(ui).showPatientInformation(patient)
    verify(ui).showBloodSugars(emptyPagingData)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when blood sugar history is loaded, then show blood sugars`() {
    // given
    val patientUuid = UUID.fromString("dbea7636-f748-438b-b801-d401da504fb3")
    val createdAt = Instant.parse("2020-01-01T00:00:00Z")
    val recordedAt = Instant.parse("2020-01-01T00:00:00Z")

    val bloodSugarNow = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("375cc86f-c582-43dd-aa0f-c06d73ea954b"),
        patientUuid = patientUuid,
        createdAt = createdAt,
        recordedAt = recordedAt
    )

    val bloodSugar15MinutesInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("6495f97d-b1a9-42f6-9fb5-8d267e3e0633"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(15, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodSugars: PagingData<BloodSugarHistoryListItem> = PagingData.from(listOf(
        BloodSugarHistoryListItem.BloodSugarHistoryItem(
            measurement = bloodSugarNow,
            isBloodSugarEditable = true,
            bloodSugarUnitPreference = BloodSugarUnitPreference.Mg,
            bloodSugarDate = "1-Jan-2020",
            bloodSugarTime = null
        ),
        BloodSugarHistoryListItem.BloodSugarHistoryItem(
            measurement = bloodSugar15MinutesInPast,
            bloodSugarUnitPreference = BloodSugarUnitPreference.Mg,
            isBloodSugarEditable = false,
            bloodSugarDate = "31-Dec-2019",
            bloodSugarTime = "12:00 AM"
        ),
    ))

    // when
    renderer.render(defaultModel.bloodSugarLoaded(bloodSugars))

    // then
    verify(ui).showBloodSugars(bloodSugars)
    verifyNoMoreInteractions(ui)
  }
}
