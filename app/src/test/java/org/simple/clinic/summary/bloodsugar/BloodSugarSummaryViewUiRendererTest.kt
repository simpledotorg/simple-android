package org.simple.clinic.summary.bloodsugar

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
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
}
