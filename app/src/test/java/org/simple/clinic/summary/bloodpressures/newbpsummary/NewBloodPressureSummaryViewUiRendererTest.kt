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
  private val uiRenderer = NewBloodPressureSummaryViewUiRenderer(ui)
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
    uiRenderer.render(model.bloodPressuresLoaded(bloodPressures))

    // then
    verify(ui).showBloodPressures(bloodPressures)
    verifyNoMoreInteractions(ui)
  }
}
