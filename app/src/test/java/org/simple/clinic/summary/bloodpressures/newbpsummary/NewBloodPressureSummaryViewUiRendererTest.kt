package org.simple.clinic.summary.bloodpressures.newbpsummary

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class NewBloodPressureSummaryViewUiRendererTest {
  private val patientUuid = UUID.fromString("8b298cc4-da11-4df9-a318-01e113f3abe3")
  private val ui = mock<NewBloodPressureSummaryViewUi>()
  private val config = NewBloodPressureSummaryViewConfig(3)
  private val uiRenderer = NewBloodPressureSummaryViewUiRenderer(ui, config)
  private val defaultModel = NewBloodPressureSummaryViewModel.create(patientUuid)

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

    // when
    uiRenderer.render(defaultModel.bloodPressuresLoaded(bloodPressures))

    // then
    verify(ui).showNoBloodPressuresView()
    verify(ui).hideSeeAllButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when blood pressures are loaded, then show blood pressures`() {
    // given
    val bloodPressure = PatientMocker.bp(
        uuid = UUID.fromString("58ff9789-c295-41ca-bab3-becb4e9b7861"),
        patientUuid = patientUuid
    )
    val bloodPressures = listOf(bloodPressure)

    // when
    uiRenderer.render(defaultModel.bloodPressuresLoaded(bloodPressures))

    // then
    verify(ui).showBloodPressures(bloodPressures)
    verify(ui).hideSeeAllButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show see all button if blood pressures count is more than number of blood pressures to display`() {
    // given
    val bloodPressure1 = PatientMocker.bp(
        uuid = UUID.fromString("f0b79c3f-b57d-4c1f-85c9-3c0045412aac"),
        patientUuid = patientUuid
    )
    val bloodPressure2 = PatientMocker.bp(
        uuid = UUID.fromString("7057728a-2021-459f-9aa4-d659c0404189"),
        patientUuid = patientUuid
    )
    val bloodPressure3 = PatientMocker.bp(
        uuid = UUID.fromString("870c83d7-b554-455d-9289-373eb3de1644"),
        patientUuid = patientUuid
    )
    val bloodPressure4 = PatientMocker.bp(
        uuid = UUID.fromString("6c69e196-a3a0-442b-a6ca-d9d20d2cd8c0"),
        patientUuid = patientUuid
    )
    val bloodPressures = listOf(bloodPressure1, bloodPressure2, bloodPressure3, bloodPressure4)
    val bloodPressuresCount = bloodPressures.size

    // when
    uiRenderer.render(
        defaultModel
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
    val bloodPressure1 = PatientMocker.bp(
        uuid = UUID.fromString("462842bc-fd28-4d34-9102-61556e1cb9e0"),
        patientUuid = patientUuid
    )
    val bloodPressure2 = PatientMocker.bp(
        uuid = UUID.fromString("aabcfc40-f114-4453-9876-1707f61676db"),
        patientUuid = patientUuid
    )
    val bloodPressure3 = PatientMocker.bp(
        uuid = UUID.fromString("0c782d93-c62c-4823-bb70-5e8b355c8b89"),
        patientUuid = patientUuid
    )
    val bloodPressures = listOf(bloodPressure1, bloodPressure2, bloodPressure3)
    val bloodPressureCount = bloodPressures.size

    // when
    uiRenderer.render(
        defaultModel
            .bloodPressuresLoaded(bloodPressures)
            .bloodPressuresCountLoaded(bloodPressureCount)
    )

    // then
    verify(ui).showBloodPressures(bloodPressures)
    verify(ui).hideSeeAllButton()
    verifyNoMoreInteractions(ui)
  }
}
