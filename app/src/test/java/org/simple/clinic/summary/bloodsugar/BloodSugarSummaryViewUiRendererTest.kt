package org.simple.clinic.summary.bloodsugar

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class BloodSugarSummaryViewUiRendererTest {

  private val ui = mock<BloodSugarSummaryViewUi>()
  private val renderer = BloodSugarSummaryViewUiRenderer(ui)
  private val patientUuid = UUID.fromString("9dd563b5-99a5-4f43-b3ab-47c43ed5d62c")
  private val defaultModel = BloodSugarSummaryViewModel.create(patientUuid)

  @Test
  fun `when blood sugar summary is being fetched then do nothing`() {
    //when
    renderer.render(defaultModel)

    //then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when blood sugar summary is fetched, then show it on the ui`() {
    //given
    val bloodSugars = listOf(PatientMocker.bloodSugar(UUID.fromString("6394f187-1e2b-454b-90bf-ed7bb55207ed")))

    //when
    renderer.render(defaultModel.summaryFetched(bloodSugars))

    //then
    verify(ui).showBloodSugarSummary(bloodSugars)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when blood sugar summary is empty then show empty ui state`() {
    //when
    renderer.render(defaultModel.summaryFetched(emptyList()))

    //then
    verify(ui).showNoBloodSugarsView()
    verifyNoMoreInteractions(ui)
  }
}
