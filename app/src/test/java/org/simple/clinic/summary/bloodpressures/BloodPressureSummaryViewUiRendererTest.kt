package org.simple.clinic.summary.bloodpressures

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.FacilityConfig
import java.util.UUID

class BloodPressureSummaryViewUiRendererTest {
  private val patientUuid = UUID.fromString("8b298cc4-da11-4df9-a318-01e113f3abe3")
  private val ui = mock<BloodPressureSummaryViewUi>()
  private val config = BloodPressureSummaryViewConfig(numberOfBpsToDisplay = 3, numberOfBpsToDisplayWithoutDiabetesManagement = 8)
  private val uiRenderer = BloodPressureSummaryViewUiRenderer(ui, config)
  private val defaultModel = BloodPressureSummaryViewModel.create(patientUuid)
  private val facility = TestData.facility(uuid = UUID.fromString("a8620f0c-dbf5-452f-84c0-9e8c3bfb3b34"))

  @Test
  fun `when blood pressures are loading, then do nothing`() {
    // when
    uiRenderer.render(defaultModel)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when loaded blood pressures are empty, then show no blood pressures view`() {
    // given
    val bloodPressures = listOf<BloodPressureMeasurement>()
    val bloodPressuresCount = 0

    // when
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(facility)
            .bloodPressuresCountLoaded(bloodPressuresCount)
            .bloodPressuresLoaded(bloodPressures)
    )

    // then
    verify(ui).showNoBloodPressuresView()
    verify(ui).hideSeeAllButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when blood pressures are loaded, then show blood pressures`() {
    // given
    val bloodPressure = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("58ff9789-c295-41ca-bab3-becb4e9b7861"),
        patientUuid = patientUuid
    )
    val bloodPressures = listOf(bloodPressure)
    val bloodPressuresCount = bloodPressures.size

    // when
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(facility)
            .bloodPressuresCountLoaded(bloodPressuresCount)
            .bloodPressuresLoaded(bloodPressures)
    )

    // then
    verify(ui).showBloodPressures(bloodPressures)
    verify(ui).hideSeeAllButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show see all button if blood pressures count is more than number of blood pressures to display with diabetes management enabled`() {
    // given
    val bloodPressure1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("f0b79c3f-b57d-4c1f-85c9-3c0045412aac"),
        patientUuid = patientUuid
    )
    val bloodPressure2 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("7057728a-2021-459f-9aa4-d659c0404189"),
        patientUuid = patientUuid
    )
    val bloodPressure3 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("870c83d7-b554-455d-9289-373eb3de1644"),
        patientUuid = patientUuid
    )
    val bloodPressure4 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("6c69e196-a3a0-442b-a6ca-d9d20d2cd8c0"),
        patientUuid = patientUuid
    )
    val bloodPressures = listOf(bloodPressure1, bloodPressure2, bloodPressure3, bloodPressure4)
    val bloodPressuresCount = bloodPressures.size
    val facility = TestData.facility(
        uuid = UUID.fromString("44058e8e-c308-4ada-8302-b0516f7f71b0"),
        facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
    )

    // when
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(facility)
            .bloodPressuresLoaded(bloodPressures)
            .bloodPressuresCountLoaded(bloodPressuresCount)
    )

    // then
    verify(ui).showBloodPressures(bloodPressures)
    verify(ui).showSeeAllButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show see all button if blood pressures count is more than number of blood pressures to display without diabetes management enabled`() {
    // given
    val bloodPressure1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("f0b79c3f-b57d-4c1f-85c9-3c0045412aac"),
        patientUuid = patientUuid
    )
    val bloodPressure2 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("7057728a-2021-459f-9aa4-d659c0404189"),
        patientUuid = patientUuid
    )
    val bloodPressure3 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("870c83d7-b554-455d-9289-373eb3de1644"),
        patientUuid = patientUuid
    )
    val bloodPressure4 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("6c69e196-a3a0-442b-a6ca-d9d20d2cd8c0"),
        patientUuid = patientUuid
    )
    val bloodPressure5 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("4b89f389-237c-487b-a896-d231a006ba42"),
        patientUuid = patientUuid
    )
    val bloodPressure6 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("96ccbd0c-ed8d-4b34-8a97-0c456c66dff2"),
        patientUuid = patientUuid
    )
    val bloodPressure7 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("a48cbe0e-fa9c-41f5-ac1b-d000d3bab245"),
        patientUuid = patientUuid
    )
    val bloodPressure8 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("bdc062d5-3c12-4403-8a66-13a6bf1d2dae"),
        patientUuid = patientUuid
    )
    val bloodPressure9 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("22416c61-e0c4-4d4d-8f49-6607c58b026c"),
        patientUuid = patientUuid
    )
    val bloodPressures = listOf(
        bloodPressure1,
        bloodPressure2,
        bloodPressure3,
        bloodPressure4,
        bloodPressure5,
        bloodPressure6,
        bloodPressure7,
        bloodPressure8,
        bloodPressure9
    )
    val bloodPressuresCount = bloodPressures.size
    val facility = TestData.facility(
        uuid = UUID.fromString("44058e8e-c308-4ada-8302-b0516f7f71b0"),
        facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
    )

    // when
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(facility)
            .bloodPressuresLoaded(bloodPressures)
            .bloodPressuresCountLoaded(bloodPressuresCount)
    )

    // then
    verify(ui).showBloodPressures(bloodPressures)
    verify(ui).showSeeAllButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `hide see all button if there are less than or equal to number of blood pressures to display`() {
    // given
    val bloodPressure1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("462842bc-fd28-4d34-9102-61556e1cb9e0"),
        patientUuid = patientUuid
    )
    val bloodPressure2 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("aabcfc40-f114-4453-9876-1707f61676db"),
        patientUuid = patientUuid
    )
    val bloodPressure3 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("0c782d93-c62c-4823-bb70-5e8b355c8b89"),
        patientUuid = patientUuid
    )
    val bloodPressures = listOf(bloodPressure1, bloodPressure2, bloodPressure3)
    val bloodPressureCount = bloodPressures.size

    // when
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(facility)
            .bloodPressuresLoaded(bloodPressures)
            .bloodPressuresCountLoaded(bloodPressureCount)
    )

    // then
    verify(ui).showBloodPressures(bloodPressures)
    verify(ui).hideSeeAllButton()
    verifyNoMoreInteractions(ui)
  }
}
